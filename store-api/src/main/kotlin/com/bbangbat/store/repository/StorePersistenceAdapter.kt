package com.bbangbat.store.repository

import com.bbangbat.store.domain.MapBounds
import com.bbangbat.store.domain.Store
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository

@Repository
class StorePersistenceAdapter(
    private val storeRepository: StoreRepository,
) {
    fun findWithinRadius(
        lat: Double,
        lng: Double,
        radiusMeters: Double,
    ): List<Store> {
        val entities: List<StoreJpaEntity?> =
            storeRepository.findAll {
                val distanceFn =
                    function(
                        Double::class,
                        "ST_Distance_Sphere",
                        function(Any::class, "POINT", path(StoreJpaEntity::longitude), path(StoreJpaEntity::latitude)),
                        function(Any::class, "POINT", value(lng), value(lat)),
                    )

                select(entity(StoreJpaEntity::class))
                    .from(entity(StoreJpaEntity::class))
                    .where(distanceFn.le(value(radiusMeters)))
                    .orderBy(distanceFn.asc())
            }

        return entities.filterNotNull().map { it.toDomain() }
    }

    fun findWithinBounds(
        bounds: MapBounds,
        limit: Int,
    ): List<Store> =
        storeRepository
            .findAllByLatitudeBetweenAndLongitudeBetween(
                bounds.south,
                bounds.north,
                bounds.west,
                bounds.east,
                PageRequest.of(0, limit),
            ).map { it.toDomain() }

    fun save(store: Store): Store = storeRepository.save(StoreJpaEntity.from(store)).toDomain()

    fun findByIdOrNull(id: Long): Store? = storeRepository.findById(id).orElse(null)?.toDomain()

    fun findAllByIds(ids: Collection<Long>): List<Store> = storeRepository.findAllById(ids).map { it.toDomain() }

    fun findAllByNameContaining(keyword: String): List<Store> =
        storeRepository.findAllByNameContainingIgnoreCase(keyword).map { it.toDomain() }
}
