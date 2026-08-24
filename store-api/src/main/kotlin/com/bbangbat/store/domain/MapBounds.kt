package com.bbangbat.store.domain

/**
 * 지도 사각 영역. 요청 범위는 서비스 지역(대전) 경계로 잘라내 과도하게 넓은 조회를 막는다.
 */
data class MapBounds(
    val south: Double,
    val north: Double,
    val west: Double,
    val east: Double,
) {
    init {
        require(south < north) { "north는 south보다 커야 합니다." }
        require(west < east) { "east는 west보다 커야 합니다." }
    }

    /** 서비스 지역과 겹치는 영역만 남긴다. 겹치는 영역이 없으면 null */
    fun clampToServiceArea(): MapBounds? {
        val clampedSouth = south.coerceIn(LAT_MIN, LAT_MAX)
        val clampedNorth = north.coerceIn(LAT_MIN, LAT_MAX)
        val clampedWest = west.coerceIn(LNG_MIN, LNG_MAX)
        val clampedEast = east.coerceIn(LNG_MIN, LNG_MAX)

        if (clampedSouth >= clampedNorth || clampedWest >= clampedEast) {
            return null
        }

        return MapBounds(clampedSouth, clampedNorth, clampedWest, clampedEast)
    }

    companion object {
        /** 영역 조회 결과 수 상한. 경계 clamp와 함께 과도하게 넓은 조회를 막는다. */
        const val MAX_RESULTS = 300

        // 대전 지역 경계
        private const val LAT_MIN = 36.19
        private const val LAT_MAX = 36.49
        private const val LNG_MIN = 127.22
        private const val LNG_MAX = 127.60
    }
}
