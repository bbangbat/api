package com.bbangbat.live.domain

import java.time.LocalDateTime

data class LiveTalkMessage(
    val id: Long = 0L,
    val storeId: Long,
    val authorId: Long,
    val authorNickname: String,
    val content: String,
    val createdAt: LocalDateTime,
) {
    init {
        require(storeId > 0) { "가게 ID가 올바르지 않습니다." }
        require(content.isNotBlank()) { "메시지는 비어 있을 수 없습니다." }
        require(content.length <= MAX_CONTENT_LENGTH) { "메시지는 ${MAX_CONTENT_LENGTH}자를 초과할 수 없습니다." }
    }

    companion object {
        const val MAX_CONTENT_LENGTH = 100
    }
}
