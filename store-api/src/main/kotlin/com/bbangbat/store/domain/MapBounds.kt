package com.bbangbat.store.domain

import kotlin.math.cos

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

        // 위도 1도의 거리. 경도 1도는 위도에 따라 좁아지므로 cos를 곱해 보정한다.
        private const val METERS_PER_LATITUDE_DEGREE = 111_320.0

        /**
         * 중심 좌표에서 반경을 감싸는 사각 영역.
         * 반경 조회 후보를 좌표 인덱스로 좁히는 용도이며, 박스는 원보다 넓으므로
         * 정확한 반경 판정은 호출부가 실제 거리로 다시 걸러야 한다.
         */
        fun around(
            latitude: Double,
            longitude: Double,
            radiusMeters: Double,
        ): MapBounds {
            val latitudeDelta = radiusMeters / METERS_PER_LATITUDE_DEGREE
            val longitudeDelta = radiusMeters / (METERS_PER_LATITUDE_DEGREE * cos(Math.toRadians(latitude)))

            return MapBounds(
                south = latitude - latitudeDelta,
                north = latitude + latitudeDelta,
                west = longitude - longitudeDelta,
                east = longitude + longitudeDelta,
            )
        }

        // 대전 지역 경계
        private const val LAT_MIN = 36.19
        private const val LAT_MAX = 36.49
        private const val LNG_MIN = 127.22
        private const val LNG_MAX = 127.60
    }
}
