package com.bbangbat.store.application.port

import com.bbangbat.store.domain.CongestionLevel

interface CongestionPort {
    fun getCongestionLevels(storeIds: List<Long>): Map<Long, CongestionLevel>
}
