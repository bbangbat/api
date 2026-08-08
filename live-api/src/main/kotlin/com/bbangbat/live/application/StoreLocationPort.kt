package com.bbangbat.live.application

/**
 * 혼잡도 투표 거리 검증에 필요한 가게 좌표를 조회하는 포트.
 * live-api는 store-api를 의존하지 않으므로 구현(어댑터)은 app 모듈에 둔다.
 */
interface StoreLocationPort {
    fun findCoordinates(storeId: Long): StoreCoordinates?
}

data class StoreCoordinates(
    val latitude: Double,
    val longitude: Double,
)
