package com.bbangbat.live.api.dto

import com.bbangbat.live.domain.StoreTalkSummary
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "톡 요약 응답")
data class StoreTalkSummaryResponse(
    @field:Schema(description = "가게 ID", example = "1") val storeId: Long,
    @field:Schema(description = "실시간 톡 한 줄 요약", example = "지금 소금빵 얘기로 뜨거워요") val summary: String,
    @field:Schema(description = "요약 갱신 시각") val updatedAt: LocalDateTime?,
) {
    companion object {
        fun from(summary: StoreTalkSummary): StoreTalkSummaryResponse =
            StoreTalkSummaryResponse(
                storeId = summary.storeId,
                summary = summary.summary,
                updatedAt = summary.updatedAt,
            )
    }
}
