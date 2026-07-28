package com.bbangbat

import com.bbangbat.review.application.ReviewStore
import com.bbangbat.review.application.StorePort
import com.bbangbat.store.application.StoreService
import org.springframework.stereotype.Component

@Component
class StorePortAdapter(
    private val storeService: StoreService,
) : StorePort {
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
}
