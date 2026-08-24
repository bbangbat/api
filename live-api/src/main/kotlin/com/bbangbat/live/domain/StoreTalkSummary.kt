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

    /** 새 요약으로 교체한다. 어디까지 요약했는지(lastMessageId)도 함께 옮긴다. */
    fun update(
        summary: String,
        lastMessageId: Long,
    ): StoreTalkSummary = copy(summary = summary, lastMessageId = lastMessageId)

    /** 마지막 요약 이후 새 톡이 없으면 다시 요약할 필요가 없다. */
    fun isUpToDate(latestMessageId: Long): Boolean = lastMessageId >= latestMessageId

    companion object {
        /** 요약 대상 선정 윈도우. 이 시간 내 톡만 활성도 판단에 쓴다. */
        const val WINDOW_MINUTES = 60L

        /** 요약 대상이 되기 위한 윈도우 내 최소 톡 수 */
        const val MIN_MESSAGES = 5L

        /** 한 번의 요약에 넘기는 최대 톡 수 (최신순으로 잘라낸다) */
        const val MAX_MESSAGES = 100

        /** 활성도 판단 대상이 되는 가장 이른 작성 시각 */
        fun windowStart(now: LocalDateTime): LocalDateTime = now.minusMinutes(WINDOW_MINUTES)
    }
}
