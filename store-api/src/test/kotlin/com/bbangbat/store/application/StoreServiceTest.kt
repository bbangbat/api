package com.bbangbat.store.application

import com.bbangbat.store.domain.Store
import com.bbangbat.store.repository.StoreRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class StoreServiceTest {
    @Mock
    private lateinit var storeRepository: StoreRepository

    private lateinit var storeService: StoreService

    @BeforeEach
    fun setUp() {
        storeService = StoreService(storeRepository)
    }

    @Test
    fun `findStores는 반경 내 가게 목록을 거리순으로 반환한다`() {
        // given
        val lat = 37.5665
        val lng = 126.9780
        val stores =
            listOf(
                Store(id = 1L, name = "가까운 베이커리", latitude = 37.5670, longitude = 126.9780, address = "서울시 중구"),
                Store(id = 2L, name = "먼 베이커리", latitude = 37.5700, longitude = 126.9780, address = "서울시 중구"),
            )
        given(storeRepository.findWithinRadius(lat, lng, 3000.0)).willReturn(stores)

        // when
        val result = storeService.findStores(lat, lng)

        // then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("가까운 베이커리")
        assertThat(result[1].name).isEqualTo("먼 베이커리")
    }

    @Test
    fun `findStores는 반경 내 가게가 없으면 빈 리스트를 반환한다`() {
        // given
        val lat = 37.5665
        val lng = 126.9780
        given(storeRepository.findWithinRadius(lat, lng, 3000.0)).willReturn(emptyList())

        // when
        val result = storeService.findStores(lat, lng)

        // then
        assertThat(result).isEmpty()
    }
}
