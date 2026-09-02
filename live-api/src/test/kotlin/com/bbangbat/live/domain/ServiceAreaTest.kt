package com.bbangbat.live.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ServiceAreaTest {
    @Test
    fun `대전 지역 내 좌표는 true를 반환한다`() {
        val lat = 36.3504
        val lng = 127.3845

        assertThat(ServiceArea.contains(lat, lng)).isTrue()
    }

    @Test
    fun `대전 지역 밖 좌표는 false를 반환한다`() {
        val lat = 37.5665
        val lng = 126.9780

        assertThat(ServiceArea.contains(lat, lng)).isFalse()
    }
}
