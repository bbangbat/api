package com.bbangbat.live.domain

import com.bbangbat.auth.voter.VoterType
import java.time.LocalDateTime

data class CongestionVote(
    val id: Long = 0L,
    val storeId: Long,
    val level: CongestionLevel,
    val voterType: VoterType,
    val voterKey: String,
    val votedAt: LocalDateTime,
) {
    init {
        require(storeId > 0) { "가게 ID가 올바르지 않습니다." }
        require(voterKey.isNotBlank()) { "투표자 식별값은 비어 있을 수 없습니다." }
    }

    /** 같은 투표자가 혼잡도를 다시 선택한다. 집계 윈도우 기준 시각(votedAt)도 함께 갱신된다. */
    fun revote(
        level: CongestionLevel,
        votedAt: LocalDateTime,
    ): CongestionVote = copy(level = level, votedAt = votedAt)
}
