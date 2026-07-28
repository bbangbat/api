package com.bbangbat.live.repository

import com.bbangbat.live.domain.CongestionLevel

/**
 * 가게별·혼잡도별 투표 수 집계 결과 (JDSL group by 프로젝션).
 */
data class StoreCongestionCount(
    val storeId: Long,
    val level: CongestionLevel,
    val count: Long,
)
