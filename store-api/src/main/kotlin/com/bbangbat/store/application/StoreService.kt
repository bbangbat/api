package com.bbangbat.store.application

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
            .findWithinRadius(lat, lng, RADIUS_METERS)
            .map { store -> withDefaultImage(store) }

    fun findByIds(storeIds: Collection<Long>): List<Store> =
        storePersistenceAdapter
            .findAllByIds(storeIds)
            .map { store -> withDefaultImage(store) }

    private fun withDefaultImage(store: Store): Store = store.copy(imageUrl = store.imageUrl ?: defaultImageUrl)

    companion object {
        private const val RADIUS_METERS = 3000.0
    }
}
