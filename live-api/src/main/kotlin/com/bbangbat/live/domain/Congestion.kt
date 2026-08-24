package com.bbangbat.live.domain

import java.time.LocalDateTime

data class Congestion(
    val storeId: Long,
    val counts: Map<CongestionLevel, Int>,
) {
    val totalVotes: Int = counts.values.sum()

    // 최다 득표 혼잡도. 동점이면 더 혼잡한 쪽(ordinal이 큰 쪽)을 우선한다. 투표가 없으면 기본값 UNCROWDED(여유).
    val current: CongestionLevel =
        counts
            .filterValues { it > 0 }
            .maxWithOrNull(compareBy({ it.value }, { it.key.ordinal }))
            ?.key
            ?: CongestionLevel.UNCROWDED

    companion object {
        /** 집계 윈도우. 이 시간 내에 들어온 투표만 현재 혼잡도로 집계한다. */
        const val WINDOW_MINUTES = 15L

        /** 집계 대상이 되는 가장 이른 투표 시각 */
        fun windowStart(now: LocalDateTime): LocalDateTime = now.minusMinutes(WINDOW_MINUTES)

        fun summarizeVotes(
            storeId: Long,
            votes: List<CongestionVote>,
        ): Congestion = summarizeCounts(storeId, votes.groupingBy { it.level }.eachCount())

        fun summarizeCounts(
            storeId: Long,
            counts: Map<CongestionLevel, Int>,
        ): Congestion {
            val normalized = CongestionLevel.entries.associateWith { counts[it] ?: 0 }

            return Congestion(storeId = storeId, counts = normalized)
        }
    }
}
