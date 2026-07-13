package com.bbangbat.live.api.dto

import com.bbangbat.live.domain.CongestionLevel
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull

@Schema(description = "혼잡도 투표 요청")
data class CongestionVoteRequest(
    @field:Schema(description = "가게 ID", example = "1")
    @field:NotNull(message = "가게 ID는 필수입니다.")
    val storeId: Long?,
    @field:Schema(description = "혼잡도", example = "CROWDED")
    @field:NotNull(message = "혼잡도는 필수입니다.")
    val level: CongestionLevel?,
    @field:Schema(description = "현재 위치 위도", example = "36.3504")
    @field:NotNull(message = "위도는 필수입니다.")
    @field:DecimalMin(value = "-90.0", message = "위도는 -90 ~ 90 사이여야 합니다.")
    @field:DecimalMax(value = "90.0", message = "위도는 -90 ~ 90 사이여야 합니다.")
    val latitude: Double?,
    @field:Schema(description = "현재 위치 경도", example = "127.3845")
    @field:NotNull(message = "경도는 필수입니다.")
    @field:DecimalMin(value = "-180.0", message = "경도는 -180 ~ 180 사이여야 합니다.")
    @field:DecimalMax(value = "180.0", message = "경도는 -180 ~ 180 사이여야 합니다.")
    val longitude: Double?,
)
