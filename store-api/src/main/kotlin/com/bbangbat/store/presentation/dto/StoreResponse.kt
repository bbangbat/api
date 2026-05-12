package com.bbangbat.store.presentation.dto

import com.bbangbat.store.domain.CongestionLevel
import com.bbangbat.store.domain.Store
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "가게 응답")
data class StoreResponse(
    @field:Schema(description = "가게 ID", example = "1234567890") val id: Long,
    @field:Schema(description = "가게명", example = "홍길동 베이커리") val name: String,
    @field:Schema(description = "위도", example = "37.5665") val latitude: Double,
    @field:Schema(description = "경도", example = "126.9780") val longitude: Double,
    @field:Schema(description = "주소", example = "서울시 강남구 테헤란로 1") val address: String,
    @field:Schema(description = "전화번호", example = "02-1234-5678") val phoneNumber: String?,
    @field:Schema(description = "혼잡도", example = "LOW") val congestionLevel: CongestionLevel,
) {
    companion object {
        fun from(
            store: Store,
            congestionLevel: CongestionLevel,
        ): StoreResponse =
            StoreResponse(
                id = store.id,
                name = store.name,
                latitude = store.latitude,
                longitude = store.longitude,
                address = store.address,
                phoneNumber = store.phoneNumber,
                congestionLevel = congestionLevel,
            )
    }
}
