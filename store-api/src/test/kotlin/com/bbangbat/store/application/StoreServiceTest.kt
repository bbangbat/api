package com.bbangbat.store.application

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.CONGESTION_UNAVAILABLE
import com.bbangbat.store.application.port.CongestionPort
import com.bbangbat.store.domain.CongestionLevel
import com.bbangbat.store.domain.Store
import com.bbangbat.store.repository.StoreRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension::class)
class StoreServiceTest {
    @Mock
    private lateinit var storeRepository: StoreRepository

    @Mock
    private lateinit var congestionPort: CongestionPort

    private lateinit var storeService: StoreService

    @BeforeEach
    fun setUp() {
        storeService = StoreService(storeRepository, congestionPort)
    }

    @Test
    fun `findStoresWithin3km은 반경 내 가게 목록과 혼잡도를 함께 반환한다`() {
        // given
        val lat = 37.5665
        val lng = 126.9780
        val stores = listOf(
            Store(id = 1L, name = "가 베이커리", latitude = 37.5650, longitude = 126.9750, address = "서울시 중구"),
            Store(id = 2L, name = "나 베이커리", latitude = 37.5700, longitude = 126.9800, address = "서울시 중구"),
        )
        given(storeRepository.findWithinRadius(lat, lng, 3000.0)).willReturn(stores)
        given(congestionPort.getCongestionLevels(listOf(1L, 2L))).willReturn(
            mapOf(1L to CongestionLevel.LOW, 2L to CongestionLevel.HIGH),
        )

        // when
        val result = storeService.findStoresWithin3km(lat, lng)

        // then
        assertThat(result).hasSize(2)
        assertThat(result[0].first.name).isEqualTo("가 베이커리")
        assertThat(result[0].second).isEqualTo(CongestionLevel.LOW)
        assertThat(result[1].first.name).isEqualTo("나 베이커리")
        assertThat(result[1].second).isEqualTo(CongestionLevel.HIGH)
    }

    @Test
    fun `findStoresWithin3km은 혼잡도 데이터가 없는 가게를 UNKNOWN으로 반환한다`() {
        // given
        val lat = 37.5665
        val lng = 126.9780
        val stores = listOf(
            Store(id = 1L, name = "가 베이커리", latitude = 37.5650, longitude = 126.9750, address = "서울시 중구"),
        )
        given(storeRepository.findWithinRadius(lat, lng, 3000.0)).willReturn(stores)
        given(congestionPort.getCongestionLevels(listOf(1L))).willReturn(emptyMap())

        // when
        val result = storeService.findStoresWithin3km(lat, lng)

        // then
        assertThat(result[0].second).isEqualTo(CongestionLevel.UNKNOWN)
    }

    @Test
    fun `findStoresWithin3km은 유효하지 않은 위도 요청 시 예외를 던진다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            storeService.findStoresWithin3km(91.0, 126.9780)
        }
    }

    @Test
    fun `findStoresWithin3km은 유효하지 않은 경도 요청 시 예외를 던진다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            storeService.findStoresWithin3km(37.5665, 181.0)
        }
    }

    @Test
    fun `findStoresWithin3km은 혼잡도 조회 실패 시 예외를 던진다`() {
        // given
        val lat = 37.5665
        val lng = 126.9780
        val stores = listOf(
            Store(id = 1L, name = "가 베이커리", latitude = 37.5650, longitude = 126.9750, address = "서울시 중구"),
        )
        given(storeRepository.findWithinRadius(lat, lng, 3000.0)).willReturn(stores)
        given(congestionPort.getCongestionLevels(listOf(1L))).willThrow(BbangbatException(CONGESTION_UNAVAILABLE))

        // when & then
        assertThrows<BbangbatException> {
            storeService.findStoresWithin3km(lat, lng)
        }
    }
}
