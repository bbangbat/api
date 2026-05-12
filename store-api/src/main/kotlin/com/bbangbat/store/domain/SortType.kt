package com.bbangbat.store.domain

enum class SortType {
    DISTANCE,   // 거리순 (기본값)
    CONGESTION, // 혼잡도순 (여유 → 보통 → 혼잡)
}
