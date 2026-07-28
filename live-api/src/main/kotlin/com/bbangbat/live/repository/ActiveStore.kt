package com.bbangbat.live.repository

/**
 * 요약 대상 활성 가게: 집계 윈도우 내 톡이 임계치 이상인 가게와 그 최신 메시지 ID.
 * latestMessageId로 "마지막 요약 이후 새 톡이 있는지"를 판정한다.
 */
data class ActiveStore(
    val storeId: Long,
    val latestMessageId: Long,
)
