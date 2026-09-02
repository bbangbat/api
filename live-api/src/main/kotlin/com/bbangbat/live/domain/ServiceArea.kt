package com.bbangbat.live.domain

object ServiceArea {
    private val LATITUDE_RANGE = 36.19..36.49
    private val LONGITUDE_RANGE = 127.22..127.60

    fun contains(
        latitude: Double,
        longitude: Double,
    ): Boolean = latitude in LATITUDE_RANGE && longitude in LONGITUDE_RANGE
}
