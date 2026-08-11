package com.bbangbat.store.application

import org.springframework.stereotype.Service

/**
 * 가게에 붙는 다른 도메인 집계.
 *
 * [StoreService]는 review-api가 자기 포트를 통해 이미 의존하고 있어서, 거기에 [ReviewPort]까지 물리면
 * store -> review -> store 순환 참조가 된다. 집계만 따로 떼어 컨트롤러에서 조합한다.
 */
@Service
class StoreStatsService(
    private val reviewPort: ReviewPort,
) {
    /** 가게 ID → 빵명록 수. 빵명록이 없는 가게는 0으로 채운다. */
    fun reviewCounts(storeIds: Collection<Long>): Map<Long, Long> {
        if (storeIds.isEmpty()) {
            return emptyMap()
        }

        val counts = reviewPort.countByStoreIds(storeIds)

        return storeIds.associateWith { counts[it] ?: 0L }
    }
}
