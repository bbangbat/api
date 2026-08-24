package com.bbangbat.store.application

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.STORE_NOT_FOUND
import com.bbangbat.common.geo.GeoDistance
import com.bbangbat.store.domain.MapBounds
import com.bbangbat.store.domain.Store
import com.bbangbat.store.repository.StorePersistenceAdapter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class StoreService(
    private val storePersistenceAdapter: StorePersistenceAdapter,
    @param:Value("\${app.store.default-image-url}") private val defaultImageUrl: String,
) {
    /**
     * 내 주변 가게 조회. 좌표 인덱스를 타는 사각 영역으로 후보를 좁힌 뒤,
     * 실제 거리(Haversine)로 반경 밖을 걸러내고 가까운 순으로 정렬한다.
     * 거리 함수를 WHERE에 두면 인덱스를 타지 못해 전체 스캔이 된다.
     */
    fun findStores(
        lat: Double,
        lng: Double,
    ): List<Store> {
        val bounds = MapBounds.around(lat, lng, Store.SEARCH_RADIUS_METERS)

        return storePersistenceAdapter
            .findAllWithinBounds(bounds)
            .map { store -> store to GeoDistance.meters(lat, lng, store.latitude, store.longitude) }
            .filter { (_, distance) -> distance <= Store.SEARCH_RADIUS_METERS }
            .sortedBy { (_, distance) -> distance }
            .map { (store, _) -> withDefaultImage(store) }
    }

    fun findById(storeId: Long): Store {
        val store = storePersistenceAdapter.findByIdOrNull(storeId) ?: throw BbangbatException(STORE_NOT_FOUND)

        return withDefaultImage(store)
    }

    /**
     * 지도 사각 영역 내 가게 조회. 요청 범위는 서비스 지역(대전)으로 잘라내고 결과 수를 제한한다.
     */
    fun findInBounds(bounds: MapBounds): List<Store> {
        val clamped = bounds.clampToServiceArea() ?: return emptyList()

        return storePersistenceAdapter
            .findWithinBounds(clamped, MapBounds.MAX_RESULTS)
            .map { store -> withDefaultImage(store) }
    }

    fun findByIds(storeIds: Collection<Long>): List<Store> {
        if (storeIds.isEmpty()) {
            return emptyList()
        }

        return storePersistenceAdapter
            .findAllByIds(storeIds)
            .map { store -> withDefaultImage(store) }
    }

    private fun withDefaultImage(store: Store): Store = store.copy(imageUrl = store.imageUrl ?: defaultImageUrl)
}
