package com.bbangbat.live.application

import com.bbangbat.live.client.TalkSummaryClient
import com.bbangbat.live.domain.LiveTalkMessage
import com.bbangbat.live.domain.StoreTalkSummary
import com.bbangbat.live.repository.ActiveStore
import com.bbangbat.live.repository.TalkPersistenceAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.then
import org.mockito.kotlin.verifyNoInteractions
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class TalkServiceTest {
    @Mock
    private lateinit var talkPersistenceAdapter: TalkPersistenceAdapter

    @Mock
    private lateinit var talkSummaryClient: TalkSummaryClient

    @Mock
    private lateinit var memberPort: MemberPort

    private lateinit var talkService: TalkService

    @BeforeEach
    fun setUp() {
        talkService = TalkService(talkPersistenceAdapter, talkSummaryClient, memberPort)
    }

    @Test
    fun `회원이 작성한 톡 수를 조회한다`() {
        given(talkPersistenceAdapter.countByAuthorId(1L)).willReturn(4L)

        val result = talkService.countByAuthorId(1L)

        assertThat(result).isEqualTo(4L)
    }

    @Test
    fun `메시지 전송 시 작성자 닉네임을 스냅샷으로 저장한다`() {
        // given
        val storeId = 1L
        val authorId = 10L
        given(memberPort.getNickname(authorId)).willReturn("빵순이")
        given(talkPersistenceAdapter.saveMessage(any())).willAnswer { it.arguments[0] }

        // when
        val result = talkService.sendMessage(storeId, authorId, "지금 사람 많아요!")

        // then
        val captor = argumentCaptor<LiveTalkMessage>()
        then(talkPersistenceAdapter).should().saveMessage(captor.capture())
        assertThat(captor.firstValue.authorNickname).isEqualTo("빵순이")
        assertThat(captor.firstValue.storeId).isEqualTo(storeId)
        assertThat(captor.firstValue.authorId).isEqualTo(authorId)
        assertThat(result.authorNickname).isEqualTo("빵순이")
    }

    @Test
    fun `afterId가 없으면 최근 24시간 윈도우 내 메시지를 조회한다`() {
        // given
        val storeId = 1L
        given(talkPersistenceAdapter.findRecentMessages(eq(storeId), any(), eq(null))).willReturn(emptyList())

        // when
        talkService.getMessages(storeId, null)

        // then
        then(talkPersistenceAdapter).should().findRecentMessages(eq(storeId), any(), eq(null))
    }

    @Test
    fun `afterId가 있으면 해당 ID 이후 메시지만 조회한다`() {
        // given
        val storeId = 1L
        val afterId = 5L
        given(talkPersistenceAdapter.findRecentMessages(eq(storeId), any(), eq(afterId))).willReturn(
            listOf(message(storeId, id = 6L)),
        )

        // when
        val result = talkService.getMessages(storeId, afterId)

        // then
        then(talkPersistenceAdapter).should().findRecentMessages(eq(storeId), any(), eq(afterId))
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(6L)
    }

    @Test
    fun `활성 가게 중 마지막 요약 이후 새 톡이 있는 가게만 요약을 요청한다`() {
        // given
        val newTalkStore = ActiveStore(storeId = 1L, latestMessageId = 100L) // 요약 없음 → 요청
        val unchangedStore = ActiveStore(storeId = 2L, latestMessageId = 50L) // 마지막 요약과 동일 → 스킵
        given(talkPersistenceAdapter.findActiveStores(any(), eq(5L))).willReturn(listOf(newTalkStore, unchangedStore))
        given(talkPersistenceAdapter.findSummaryByStoreId(1L)).willReturn(null)
        given(talkPersistenceAdapter.findSummaryByStoreId(2L)).willReturn(
            StoreTalkSummary(storeId = 2L, summary = "기존 요약", lastMessageId = 50L),
        )
        given(talkPersistenceAdapter.findRecentMessages(eq(1L), any(), eq(null))).willReturn(
            listOf(message(1L, id = 100L)),
        )

        // when
        talkService.summarizeActiveStores()

        // then
        then(talkSummaryClient).should().requestSummary(eq(1L), eq(100L), any())
        then(talkSummaryClient).should(never()).requestSummary(eq(2L), any(), any())
    }

    @Test
    fun `빈 storeIds면 요약을 조회하지 않고 빈 리스트를 반환한다`() {
        // when
        val result = talkService.getSummaries(emptyList())

        // then
        assertThat(result).isEmpty()
        verifyNoInteractions(talkPersistenceAdapter)
    }

    @Test
    fun `storeIds로 요약을 벌크 조회한다`() {
        // given
        val storeIds = listOf(1L, 2L)
        given(talkPersistenceAdapter.findSummariesByStoreIds(storeIds)).willReturn(
            listOf(
                StoreTalkSummary(storeId = 1L, summary = "요약1", lastMessageId = 10L),
                StoreTalkSummary(storeId = 2L, summary = "요약2", lastMessageId = 20L),
            ),
        )

        // when
        val result = talkService.getSummaries(storeIds)

        // then
        assertThat(result).hasSize(2)
        assertThat(result.map { it.storeId }).containsExactly(1L, 2L)
    }

    private fun message(
        storeId: Long,
        id: Long = 1L,
    ): LiveTalkMessage =
        LiveTalkMessage(
            id = id,
            storeId = storeId,
            authorId = 1L,
            authorNickname = "빵순이",
            content = "지금 사람 많아요!",
            createdAt = LocalDateTime.now(),
        )
}
