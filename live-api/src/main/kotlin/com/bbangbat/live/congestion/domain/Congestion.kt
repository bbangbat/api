package com.bbangbat.live.congestion.domain

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
