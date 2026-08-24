package com.bbangbat.common.geo

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 두 좌표 사이의 실제 거리 계산 (Haversine).
 * store-api(반경 조회)와 live-api(투표 거리 검증)가 함께 쓰므로 common에 둔다.
 */
object GeoDistance {
    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun meters(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double,
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a =
            sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)

        return 2 * EARTH_RADIUS_METERS * asin(sqrt(a))
    }
}
