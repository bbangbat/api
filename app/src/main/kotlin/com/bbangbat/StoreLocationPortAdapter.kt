package com.bbangbat

import com.bbangbat.live.application.StoreCoordinates
import com.bbangbat.live.application.StoreLocationPort
import com.bbangbat.store.application.StoreService
import org.springframework.stereotype.Component

@Component
class StoreLocationPortAdapter(
    private val storeService: StoreService,
) : StoreLocationPort {
    override fun findCoordinates(storeId: Long): StoreCoordinates? =
        runCatching { storeService.findById(storeId) }
            .map { StoreCoordinates(latitude = it.latitude, longitude = it.longitude) }
            .getOrNull()
}
