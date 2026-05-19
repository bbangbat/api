package com.bbangbat.store.repository

import com.bbangbat.store.domain.Store
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.stereotype.Repository

@Repository
class StoreRepository(
    private val storeJpaRepository: StoreJpaRepository,
    private val jdslExecutor: KotlinJdslJpqlExecutor,
) {

    fun findWithinRadius(
        lat: Double,
        lng: Double,
        radiusMeters: Double,
    ): List<Store> {

        return jdslExecutor.findAll {

            val distanceFn = function(
                Double::class,
                "ST_Distance_Sphere",
                function(Any::class, "POINT", path(StoreJpaEntity::longitude), path(StoreJpaEntity::latitude)),
                function(Any::class, "POINT", value(lng), value(lat)),
            )

            select(entity(StoreJpaEntity::class))
                .from(entity(StoreJpaEntity::class))
                .where(distanceFn.le(value(radiusMeters)))
                .orderBy(distanceFn.asc())

        }.filterNotNull().map { it.toDomain() }

    }

    fun save(store: Store): Store {

        return storeJpaRepository.save(StoreJpaEntity.from(store)).toDomain()

    }

}
