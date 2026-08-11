package com.bbangbat

import com.bbangbat.live.application.StoreCoordinates
import com.bbangbat.review.application.ReviewStore
import com.bbangbat.store.application.StoreService
import org.springframework.stereotype.Component
import com.bbangbat.live.application.StorePort as LiveStorePort
import com.bbangbat.review.application.StorePort as ReviewStorePort

/** 다른 모듈이 가게 정보를 필요로 할 때 쓰는 포트들의 구현 */
@Component
class StorePortAdapter(
    private val storeService: StoreService,
) : ReviewStorePort,
    LiveStorePort {
    override fun findByIds(storeIds: Collection<Long>): Map<Long, ReviewStore> =
        storeService
            .findByIds(storeIds)
            .associate { store ->
                store.id to
                    ReviewStore(
                        id = store.id,
                        name = store.name,
                        imageUrl = requireNotNull(store.imageUrl),
                    )
            }

    override fun findCoordinates(storeId: Long): StoreCoordinates? =
        runCatching { storeService.findById(storeId) }
            .map { StoreCoordinates(latitude = it.latitude, longitude = it.longitude) }
            .getOrNull()

    override fun findNames(storeIds: Collection<Long>): Map<Long, String> = storeService.findByIds(storeIds).associate { it.id to it.name }
}
