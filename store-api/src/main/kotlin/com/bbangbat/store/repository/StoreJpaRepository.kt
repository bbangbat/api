package com.bbangbat.store.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface StoreJpaRepository : JpaRepository<StoreJpaEntity, Long> {

    @Query(
        value = """
            SELECT * FROM stores
            WHERE ST_Distance_Sphere(POINT(longitude, latitude), POINT(:lng, :lat)) <= :radiusMeters
            ORDER BY ST_Distance_Sphere(POINT(longitude, latitude), POINT(:lng, :lat)) ASC
        """,
        nativeQuery = true,
    )
    fun findWithinRadius(
        @Param("lat") lat: Double,
        @Param("lng") lng: Double,
        @Param("radiusMeters") radiusMeters: Double,
    ): List<StoreJpaEntity>
}
