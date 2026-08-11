package com.bbangbat.store.application

/**
 * store-api가 빵명록 정보를 필요로 할 때 쓰는 포트.
 * store-api는 review-api를 의존하지 않으므로 구현(어댑터)은 app 모듈에 둔다.
 */
interface ReviewPort {
    /** 가게 ID → 빵명록 수. 빵명록이 없는 가게는 결과에서 빠진다. */
    fun countByStoreIds(storeIds: Collection<Long>): Map<Long, Long>
}
