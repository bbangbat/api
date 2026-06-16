package com.bbangbat.live.congestion.presentation.dto

import com.bbangbat.live.congestion.domain.Congestion
import com.bbangbat.live.congestion.domain.CongestionLevel
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "혼잡도 응답")
data class CongestionResponse(
    @field:Schema(description = "가게 ID", example = "1")
    val storeId: Long,
    @field:Schema(description = "현재 혼잡도 (최다 득표, 동점 시 더 혼잡한 쪽, 투표 없으면 UNCROWDED)", example = "CROWDED")
    val current: CongestionLevel,
    @field:Schema(description = "여유 투표 수", example = "3")
    val uncrowdedVotes: Int,
    @field:Schema(description = "보통 투표 수", example = "5")
    val normalVotes: Int,
    @field:Schema(description = "혼잡 투표 수", example = "2")
    val crowdedVotes: Int,
    @field:Schema(description = "총 투표 수", example = "10")
    val totalVotes: Int,
) {
    companion object {
        fun from(congestion: Congestion): CongestionResponse =
            CongestionResponse(
                storeId = congestion.storeId,
                current = congestion.current,
                uncrowdedVotes = congestion.counts[CongestionLevel.UNCROWDED] ?: 0,
                normalVotes = congestion.counts[CongestionLevel.NORMAL] ?: 0,
                crowdedVotes = congestion.counts[CongestionLevel.CROWDED] ?: 0,
                totalVotes = congestion.totalVotes,
            )
    }
}
