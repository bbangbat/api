package com.bbangbat.store.application

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.CONGESTION_UNAVAILABLE
import com.bbangbat.store.application.port.CongestionPort
import com.bbangbat.store.domain.CongestionLevel
import com.bbangbat.store.domain.SortType
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
    fun `findStores는 반경 내 가게 목록과 혼잡도를 함께 반환한다`() {
        // given
        val lat = 37.5665
        val lng = 126.9780
        val stores = listOf(
            Store(id = 1L, name = "가까운 베이커리", latitude = 37.5670, longitude = 126.9780, address = "서울시 중구"),
            Store(id = 2L, name = "먼 베이커리", latitude = 37.5700, longitude = 126.9780, address = "서울시 중구"),
        )
        given(storeRepository.findWithinRadius(lat, lng, 3000.0)).willReturn(stores)
        given(congestionPort.getCongestionLevels(listOf(1L, 2L))).willReturn(
            mapOf(1L to CongestionLevel.RELAXED, 2L to CongestionLevel.CROWDED),
        )

        // when
        val result = storeService.findStores(lat, lng, SortType.DISTANCE)

        // then
        assertThat(result).hasSize(2)
        assertThat(result[0].first.name).isEqualTo("가까운 베이커리")
        assertThat(result[0].second).isEqualTo(CongestionLevel.RELAXED)
        assertThat(result[1].second).isEqualTo(CongestionLevel.CROWDED)
    }

    @Test
    fun `findStores는 혼잡도 데이터가 없는 가게를 RELAXED로 반환한다`() {
        // given
        val lat = 37.5665
        val lng = 126.9780
        val stores = listOf(
            Store(id = 1L, name = "가 베이커리", latitude = 37.5670, longitude = 126.9780, address = "서울시 중구"),
        )
        given(storeRepository.findWithinRadius(lat, lng, 3000.0)).willReturn(stores)
        given(congestionPort.getCongestionLevels(listOf(1L))).willReturn(emptyMap())

        // when
        val result = storeService.findStores(lat, lng, SortType.DISTANCE)

        // then
        assertThat(result[0].second).isEqualTo(CongestionLevel.RELAXED)
    }

    @Test
    fun `findStores는 혼잡도순 정렬 시 여유-보통-혼잡 순으로 반환한다`() {
        // given
        val lat = 37.5665
        val lng = 126.9780
        val stores = listOf(
            Store(id = 1L, name = "혼잡 베이커리", latitude = 37.5670, longitude = 126.9780, address = "서울시 중구"),
            Store(id = 2L, name = "여유 베이커리", latitude = 37.5680, longitude = 126.9780, address = "서울시 중구"),
            Store(id = 3L, name = "보통 베이커리", latitude = 37.5690, longitude = 126.9780, address = "서울시 중구"),
        )
        given(storeRepository.findWithinRadius(lat, lng, 3000.0)).willReturn(stores)
        given(congestionPort.getCongestionLevels(listOf(1L, 2L, 3L))).willReturn(
            mapOf(
                1L to CongestionLevel.CROWDED,
                2L to CongestionLevel.RELAXED,
                3L to CongestionLevel.NORMAL,
            ),
        )

        // when
        val result = storeService.findStores(lat, lng, SortType.CONGESTION)

        // then
        assertThat(result.map { it.second }).containsExactly(
            CongestionLevel.RELAXED,
            CongestionLevel.NORMAL,
            CongestionLevel.CROWDED,
        )
    }

    @Test
    fun `findStores는 유효하지 않은 위도 요청 시 예외를 던진다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            storeService.findStores(91.0, 126.9780, SortType.DISTANCE)
        }
    }

    @Test
    fun `findStores는 유효하지 않은 경도 요청 시 예외를 던진다`() {
        // when & then
        assertThrows<IllegalArgumentException> {
            storeService.findStores(37.5665, 181.0, SortType.DISTANCE)
        }
    }

    @Test
    fun `findStores는 혼잡도 조회 실패 시 예외를 던진다`() {
        // given
        val lat = 37.5665
        val lng = 126.9780
        val stores = listOf(
            Store(id = 1L, name = "가 베이커리", latitude = 37.5670, longitude = 126.9780, address = "서울시 중구"),
        )
        given(storeRepository.findWithinRadius(lat, lng, 3000.0)).willReturn(stores)
        given(congestionPort.getCongestionLevels(listOf(1L))).willThrow(BbangbatException(CONGESTION_UNAVAILABLE))

        // when & then
        assertThrows<BbangbatException> {
            storeService.findStores(lat, lng, SortType.DISTANCE)
        }
    }
}
