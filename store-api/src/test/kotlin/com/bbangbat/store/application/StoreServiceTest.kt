package com.bbangbat.store.application

import com.bbangbat.store.domain.Store
import com.bbangbat.store.repository.StorePersistenceAdapter
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
    private lateinit var storePersistenceAdapter: StorePersistenceAdapter

    private lateinit var storeService: StoreService

    @BeforeEach
    fun setUp() {
        storeService = StoreService(storePersistenceAdapter, DEFAULT_IMAGE_URL)
    }

    @Test
    fun `반경 내 가게 목록을 거리순으로 반환한다`() {
        // given
        val lat = 37.5665
        val lng = 126.9780
        val stores =
            listOf(
                Store(id = 1L, name = "가까운 베이커리", latitude = 37.5670, longitude = 126.9780, address = "서울시 중구"),
                Store(id = 2L, name = "먼 베이커리", latitude = 37.5700, longitude = 126.9780, address = "서울시 중구"),
            )
        given(storePersistenceAdapter.findWithinRadius(lat, lng, 3000.0)).willReturn(stores)

        // when
        val result = storeService.findStores(lat, lng)

        // then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("가까운 베이커리")
        assertThat(result[1].name).isEqualTo("먼 베이커리")
    }

    @Test
    fun `반경 내 가게가 없으면 빈 리스트를 반환한다`() {
        // given
        val lat = 37.5665
        val lng = 126.9780
        given(storePersistenceAdapter.findWithinRadius(lat, lng, 3000.0)).willReturn(emptyList())

        // when
        val result = storeService.findStores(lat, lng)

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `이미지가 없는 가게는 기본 이미지 URL로 채운다`() {
        // given
        val lat = 37.5665
        val lng = 126.9780
        val stores =
            listOf(
                Store(
                    id = 1L,
                    name = "이미지 없는 빵집",
                    latitude = 37.5670,
                    longitude = 126.9780,
                    address = "서울시 중구",
                    imageUrl = null,
                ),
            )
        given(storePersistenceAdapter.findWithinRadius(lat, lng, 3000.0)).willReturn(stores)

        // when
        val result = storeService.findStores(lat, lng)

        // then
        assertThat(result[0].imageUrl).isEqualTo(DEFAULT_IMAGE_URL)
    }

    @Test
    fun `이미지가 있는 가게는 원래 URL을 유지한다`() {
        // given
        val lat = 37.5665
        val lng = 126.9780
        val customUrl = "store-specific-image-url"
        val stores =
            listOf(
                Store(
                    id = 1L,
                    name = "이미지 있는 빵집",
                    latitude = 37.5670,
                    longitude = 126.9780,
                    address = "서울시 중구",
                    imageUrl = customUrl,
                ),
            )
        given(storePersistenceAdapter.findWithinRadius(lat, lng, 3000.0)).willReturn(stores)

        // when
        val result = storeService.findStores(lat, lng)

        // then
        assertThat(result[0].imageUrl).isEqualTo(customUrl)
    }

    @Test
    fun `ID로 조회한 가게에도 기본 이미지를 적용한다`() {
        val storeIds = listOf(1L, 2L)
        val stores =
            listOf(
                Store(
                    id = 1L,
                    name = "이미지 없는 빵집",
                    latitude = 37.5670,
                    longitude = 126.9780,
                    address = "서울시 중구",
                    imageUrl = null,
                ),
                Store(
                    id = 2L,
                    name = "이미지 있는 빵집",
                    latitude = 37.5670,
                    longitude = 126.9780,
                    address = "서울시 중구",
                    imageUrl = "custom-image-url",
                ),
            )
        given(storePersistenceAdapter.findAllByIds(storeIds)).willReturn(stores)

        val result = storeService.findByIds(storeIds)

        assertThat(result[0].imageUrl).isEqualTo(DEFAULT_IMAGE_URL)
        assertThat(result[1].imageUrl).isEqualTo("custom-image-url")
    }

    companion object {
        private const val DEFAULT_IMAGE_URL = "default-image-url"
    }
}
