package com.bbangbat.live.domain

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class LiveTalkMessageTest {
    @Test
    fun `가게 ID가 0 이하면 예외를 던진다`() {
        assertThatThrownBy { message(storeId = 0L) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `내용이 비어 있으면 예외를 던진다`() {
        assertThatThrownBy { message(content = "   ") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `내용이 100자를 초과하면 예외를 던진다`() {
        assertThatThrownBy { message(content = "a".repeat(101)) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `내용이 100자이면 예외 없이 생성된다`() {
        message(content = "a".repeat(100))
    }

    private fun message(
        storeId: Long = 1L,
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
