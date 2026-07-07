package com.bbangbat.live.repository

import com.bbangbat.live.domain.LiveTalkMessage
import com.bbangbat.live.support.AbstractContainerBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.context.annotation.Import
import java.time.LocalDateTime

@Import(LiveTalkMessagePersistenceAdapter::class)
class LiveTalkMessagePersistenceAdapterTest
    @Autowired
    constructor(
        private val liveTalkMessagePersistenceAdapter: LiveTalkMessagePersistenceAdapter,
        private val em: TestEntityManager,
    ) : AbstractContainerBaseTest() {
        @Test
        fun `메시지를 저장하면 id와 createdAt이 채워진다`() {
            // when
            val saved = liveTalkMessagePersistenceAdapter.save(message(storeId = 1L))

            // then
            assertThat(saved.id).isGreaterThan(0L)
            assertThat(saved.createdAt).isNotNull
        }

        @Test
        fun `findRecentMessages는 윈도우 밖의 오래된 메시지를 제외한다`() {
            // given
            val old = liveTalkMessagePersistenceAdapter.save(message(storeId = 1L, content = "오래된 메시지"))
            em.flush()
            val cutoff = old.createdAt.plusNanos(1_000_000)
            Thread.sleep(10)
            liveTalkMessagePersistenceAdapter.save(message(storeId = 1L, content = "최근 메시지"))
            em.flush()
            em.clear()

            // when
            val recent = liveTalkMessagePersistenceAdapter.findRecentMessages(1L, cutoff, null)

            // then
            assertThat(recent).hasSize(1)
            assertThat(recent[0].content).isEqualTo("최근 메시지")
        }

        @Test
        fun `findRecentMessages는 다른 가게의 메시지를 제외한다`() {
            // given
            liveTalkMessagePersistenceAdapter.save(message(storeId = 1L, content = "가게1 메시지"))
            liveTalkMessagePersistenceAdapter.save(message(storeId = 2L, content = "가게2 메시지"))
            em.flush()
            em.clear()

            // when
            val recent = liveTalkMessagePersistenceAdapter.findRecentMessages(1L, LocalDateTime.now().minusMinutes(60), null)

            // then
            assertThat(recent).hasSize(1)
            assertThat(recent[0].content).isEqualTo("가게1 메시지")
        }

        @Test
        fun `afterId를 전달하면 그 이후 메시지만 오래된 순으로 반환한다`() {
            // given
            val first = liveTalkMessagePersistenceAdapter.save(message(storeId = 1L, content = "첫번째"))
            val second = liveTalkMessagePersistenceAdapter.save(message(storeId = 1L, content = "두번째"))
            val third = liveTalkMessagePersistenceAdapter.save(message(storeId = 1L, content = "세번째"))
            em.flush()
            em.clear()

            // when
            val messages = liveTalkMessagePersistenceAdapter.findRecentMessages(1L, LocalDateTime.now().minusMinutes(60), first.id)

            // then
            assertThat(messages.map { it.id }).containsExactly(second.id, third.id)
            assertThat(messages.map { it.content }).containsExactly("두번째", "세번째")
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
    }
