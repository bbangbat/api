package com.bbangbat.live.application

import com.bbangbat.live.domain.LiveTalkMessage
import com.bbangbat.live.repository.LiveTalkMessageRepository
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
import org.mockito.kotlin.then
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class LiveTalkServiceTest {
    @Mock
    private lateinit var liveTalkMessageRepository: LiveTalkMessageRepository

    @Mock
    private lateinit var memberPort: MemberPort

    private lateinit var liveTalkService: LiveTalkService

    @BeforeEach
    fun setUp() {
        liveTalkService = LiveTalkService(liveTalkMessageRepository, memberPort)
    }

    @Test
    fun `메시지 전송 시 작성자 닉네임을 스냅샷으로 저장한다`() {
        // given
        val storeId = 1L
        val authorId = 10L
        given(memberPort.getNickname(authorId)).willReturn("빵순이")
        given(liveTalkMessageRepository.save(any())).willAnswer { it.arguments[0] }

        // when
        val result = liveTalkService.sendMessage(storeId, authorId, "지금 사람 많아요!")

        // then
        val captor = argumentCaptor<LiveTalkMessage>()
        then(liveTalkMessageRepository).should().save(captor.capture())
        assertThat(captor.firstValue.authorNickname).isEqualTo("빵순이")
        assertThat(captor.firstValue.storeId).isEqualTo(storeId)
        assertThat(captor.firstValue.authorId).isEqualTo(authorId)
        assertThat(result.authorNickname).isEqualTo("빵순이")
    }

    @Test
    fun `afterId가 없으면 최근 24시간 윈도우 내 메시지를 조회한다`() {
        // given
        val storeId = 1L
        given(liveTalkMessageRepository.findRecentMessages(eq(storeId), any(), eq(null))).willReturn(emptyList())

        // when
        liveTalkService.getMessages(storeId, null)

        // then
        then(liveTalkMessageRepository).should().findRecentMessages(eq(storeId), any(), eq(null))
    }

    @Test
    fun `afterId가 있으면 해당 ID 이후 메시지만 조회한다`() {
        // given
        val storeId = 1L
        val afterId = 5L
        given(liveTalkMessageRepository.findRecentMessages(eq(storeId), any(), eq(afterId))).willReturn(
            listOf(message(storeId, id = 6L)),
        )

        // when
        val result = liveTalkService.getMessages(storeId, afterId)

        // then
        then(liveTalkMessageRepository).should().findRecentMessages(eq(storeId), any(), eq(afterId))
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(6L)
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
