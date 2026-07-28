package com.bbangbat.review.application

interface StorePort {
    fun findByIds(storeIds: Collection<Long>): Map<Long, ReviewStore>
}
