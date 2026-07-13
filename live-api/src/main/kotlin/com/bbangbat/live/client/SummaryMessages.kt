package com.bbangbat.live.client

/**
 * Spring → AI 서버 (요청 큐): 요약할 톡 메시지 묶음.
 */
data class SummaryRequestMessage(
    val storeId: Long,
    val lastMessageId: Long,
    val messages: List<String>,
)

/**
 * AI 서버 → Spring (결과 큐): 생성된 한 줄 요약.
 */
data class SummaryResultMessage(
    val storeId: Long,
    val lastMessageId: Long,
    val summary: String,
)
