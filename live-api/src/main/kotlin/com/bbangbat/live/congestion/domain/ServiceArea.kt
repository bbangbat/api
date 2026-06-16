package com.bbangbat.live.congestion.domain

/**
 * 대전 지역 경계 (바운딩 박스).
 * 혼잡도 투표는 대전 지역 내 사용자만 가능하다.
 */
object ServiceArea {
    private val LATITUDE_RANGE = 36.19..36.49
    private val LONGITUDE_RANGE = 127.22..127.60

    fun contains(
        latitude: Double,
        longitude: Double,
    ): Boolean = latitude in LATITUDE_RANGE && longitude in LONGITUDE_RANGE
}
