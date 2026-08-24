package com.bbangbat.store.application

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.STORE_NOT_FOUND
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
    fun findStores(
        lat: Double,
        lng: Double,
    ): List<Store> =
        storePersistenceAdapter
            .findWithinRadius(lat, lng, Store.SEARCH_RADIUS_METERS)
            .map { store -> withDefaultImage(store) }

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
