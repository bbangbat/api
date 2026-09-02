package com.bbangbat.live.application

interface StorePort {
    fun findCoordinates(storeId: Long): StoreCoordinates?

    fun findNames(storeIds: Collection<Long>): Map<Long, String>
}

data class StoreCoordinates(
    val latitude: Double,
    val longitude: Double,
)
