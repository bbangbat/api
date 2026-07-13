package com.bbangbat.live.domain

import java.time.LocalDateTime

data class StoreTalkSummary(
    val id: Long = 0L,
    val storeId: Long,
    val summary: String,
    val lastMessageId: Long,
    val updatedAt: LocalDateTime? = null,
) {
    init {
        require(summary.isNotBlank()) { "요약은 비어 있을 수 없습니다." }
    }
}
