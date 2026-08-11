package com.bbangbat.review.repository

/** 가게별 빵명록 수 집계 결과 (JDSL group by 프로젝션) */
data class StoreReviewCount(
    val storeId: Long,
    val count: Long,
)
