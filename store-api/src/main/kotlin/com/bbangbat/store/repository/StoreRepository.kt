package com.bbangbat.store.repository

import com.bbangbat.store.domain.Store
import org.springframework.stereotype.Repository

@Repository
class StoreRepository(
    private val storeJpaRepository: StoreJpaRepository,
) {
    fun findWithinRadius(
        lat: Double,
        lng: Double,
        radiusMeters: Double,
    ): List<Store> =
        storeJpaRepository
            .findWithinRadius(lat, lng, radiusMeters)
            .map { it.toDomain() }

    fun save(store: Store): Store = storeJpaRepository.save(StoreJpaEntity.from(store)).toDomain()
}
