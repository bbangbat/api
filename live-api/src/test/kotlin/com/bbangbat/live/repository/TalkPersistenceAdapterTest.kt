package com.bbangbat.live.repository

import com.bbangbat.live.domain.LiveTalkMessage
import com.bbangbat.live.domain.StoreTalkSummary
import com.bbangbat.live.support.AbstractContainerBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.context.annotation.Import
import java.time.LocalDateTime

@Import(TalkPersistenceAdapter::class)
class TalkPersistenceAdapterTest
    @Autowired
    constructor(
        private val talkPersistenceAdapter: TalkPersistenceAdapter,
        private val em: TestEntityManager,
    ) : AbstractContainerBaseTest() {
        // ===== 메시지 =====

        @Test
        fun `메시지를 저장하면 id와 createdAt이 채워진다`() {
            // when
            val saved = talkPersistenceAdapter.saveMessage(message(storeId = 1L))

            // then
            assertThat(saved.id).isGreaterThan(0L)
            assertThat(saved.createdAt).isNotNull
        }

        @Test
        fun `findRecentMessages는 다른 가게의 메시지를 제외한다`() {
            // given
            talkPersistenceAdapter.saveMessage(message(storeId = 1L, content = "가게1 메시지"))
            talkPersistenceAdapter.saveMessage(message(storeId = 2L, content = "가게2 메시지"))
            em.flush()
            em.clear()

            // when
            val recent = talkPersistenceAdapter.findRecentMessages(1L, LocalDateTime.now().minusMinutes(60), null)

            // then
            assertThat(recent).hasSize(1)
            assertThat(recent[0].content).isEqualTo("가게1 메시지")
        }

        // ===== 활성 가게 =====

        @Test
        fun `findActiveStores는 톡이 임계치 이상인 가게만 최신 메시지 ID와 함께 반환한다`() {
            // given (가게1: 5건, 가게2: 2건)
            repeat(5) { talkPersistenceAdapter.saveMessage(message(storeId = 1L)) }
            repeat(2) { talkPersistenceAdapter.saveMessage(message(storeId = 2L)) }
            em.flush()
            em.clear()

            // when
            val active = talkPersistenceAdapter.findActiveStores(LocalDateTime.now().minusMinutes(60), 5)

            // then
            assertThat(active).hasSize(1)
            assertThat(active[0].storeId).isEqualTo(1L)
            assertThat(active[0].latestMessageId).isGreaterThan(0L)
        }

        // ===== 요약 =====

        @Test
        fun `upsertSummary는 신규 요약을 저장한다`() {
            // when
            talkPersistenceAdapter.upsertSummary(summary(storeId = 1L, text = "요약", lastMessageId = 10L))
            em.flush()
            em.clear()

            // then
            val found = talkPersistenceAdapter.findSummaryByStoreId(1L)
            assertThat(found).isNotNull
            assertThat(found!!.summary).isEqualTo("요약")
            assertThat(found.lastMessageId).isEqualTo(10L)
        }

        @Test
        fun `upsertSummary는 같은 가게의 기존 요약을 갱신한다`() {
            // given
            talkPersistenceAdapter.upsertSummary(summary(storeId = 1L, text = "이전 요약", lastMessageId = 10L))
            em.flush()
            em.clear()

            // when
            talkPersistenceAdapter.upsertSummary(summary(storeId = 1L, text = "최신 요약", lastMessageId = 20L))
            em.flush()
            em.clear()

            // then
            val found = talkPersistenceAdapter.findSummaryByStoreId(1L)!!
            assertThat(found.summary).isEqualTo("최신 요약")
            assertThat(found.lastMessageId).isEqualTo(20L)
        }

        @Test
        fun `findSummariesByStoreIds는 요약이 있는 가게만 벌크로 반환한다`() {
            // given (가게 1,2만 요약 있음)
            talkPersistenceAdapter.upsertSummary(summary(storeId = 1L, text = "요약1", lastMessageId = 1L))
            talkPersistenceAdapter.upsertSummary(summary(storeId = 2L, text = "요약2", lastMessageId = 2L))
            em.flush()
            em.clear()

            // when
            val result = talkPersistenceAdapter.findSummariesByStoreIds(listOf(1L, 2L, 3L))

            // then
            assertThat(result).hasSize(2)
            assertThat(result.map { it.storeId }).containsExactlyInAnyOrder(1L, 2L)
        }

        private fun message(
            storeId: Long,
            content: String = "지금 사람 많아요!",
        ): LiveTalkMessage =
            LiveTalkMessage(
                storeId = storeId,
                authorId = 1L,
                authorNickname = "빵순이",
                content = content,
                createdAt = LocalDateTime.now(),
            )

        private fun summary(
            storeId: Long,
            text: String,
            lastMessageId: Long,
        ): StoreTalkSummary =
            StoreTalkSummary(
                storeId = storeId,
                summary = text,
                lastMessageId = lastMessageId,
            )
    }
