package com.bbangbat.store.application

import com.bbangbat.store.application.port.CongestionPort
import com.bbangbat.store.domain.CongestionLevel
import com.bbangbat.store.domain.Store
import com.bbangbat.store.repository.StoreRepository
import org.springframework.stereotype.Service

@Service
class StoreService(
    private val storeRepository: StoreRepository,
    private val congestionPort: CongestionPort,
) {
    fun findStoresWithin3km(lat: Double, lng: Double): List<Pair<Store, CongestionLevel>> {
        require(lat in -90.0..90.0) { "위도는 -90 ~ 90 사이여야 합니다." }
        require(lng in -180.0..180.0) { "경도는 -180 ~ 180 사이여야 합니다." }

        val stores = storeRepository.findWithinRadius(lat, lng, RADIUS_METERS)
        val congestionMap = congestionPort.getCongestionLevels(stores.map { it.id })

        return stores.map { store -> store to (congestionMap[store.id] ?: CongestionLevel.UNKNOWN) }
    }

    companion object {
        private const val RADIUS_METERS = 3000.0
    }
}
