package com.bbangbat.live.application

/**
 * live-api가 가게 정보를 필요로 할 때 쓰는 포트.
 * live-api는 store-api를 의존하지 않으므로 구현(어댑터)은 app 모듈에 둔다.
 */
interface StorePort {
    /** 혼잡도 투표 거리 검증용 좌표. 없는 가게면 null */
    fun findCoordinates(storeId: Long): StoreCoordinates?

    /** 가게 ID → 가게명. 없는 가게는 결과에서 빠진다. */
    fun findNames(storeIds: Collection<Long>): Map<Long, String>
}

data class StoreCoordinates(
    val latitude: Double,
    val longitude: Double,
)
