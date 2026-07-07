package com.bbangbat.store.application

import com.bbangbat.store.domain.Store
import com.bbangbat.store.repository.StorePersistenceAdapter
import org.springframework.stereotype.Service

@Service
class StoreService(
    private val storePersistenceAdapter: StorePersistenceAdapter,
) {
    fun findStores(
        lat: Double,
        lng: Double,
    ): List<Store> = storePersistenceAdapter.findWithinRadius(lat, lng, RADIUS_METERS)

    companion object {
        private const val RADIUS_METERS = 3000.0
    }
}
