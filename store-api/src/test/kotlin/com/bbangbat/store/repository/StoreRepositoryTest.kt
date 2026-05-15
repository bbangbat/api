package com.bbangbat.store.repository

import com.bbangbat.store.support.AbstractContainerBaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@Import(StoreRepository::class)
class StoreRepositoryTest : AbstractContainerBaseTest() {
    @Autowired
    private lateinit var storeRepository: StoreRepository

    @Autowired
    private lateinit var storeJpaRepository: StoreJpaRepository

    @BeforeEach
    fun setUp() {
        storeJpaRepository.deleteAll()
    }

    @Test
    fun `반경 내 가게를 거리순으로 조회한다`() {
        // given
        val centerLat = 37.5665
        val centerLng = 126.9780
        storeJpaRepository.saveAll(
            listOf(
                storeEntity("먼 베이커리", latitude = 37.5700, longitude = 126.9780),
                storeEntity("가까운 베이커리", latitude = 37.5670, longitude = 126.9780),
            ),
        )

        // when
        val result = storeRepository.findWithinRadius(centerLat, centerLng, 3000.0)

        // then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("가까운 베이커리")
        assertThat(result[1].name).isEqualTo("먼 베이커리")
    }

    @Test
    fun `반경 밖 가게는 조회되지 않는다`() {
        // given
        val centerLat = 37.5665
        val centerLng = 126.9780
        storeJpaRepository.saveAll(
            listOf(
                storeEntity("가까운 베이커리", latitude = 37.5700, longitude = 126.9800),
                storeEntity("먼 베이커리", latitude = 38.0000, longitude = 127.5000),
            ),
        )

        // when
        val result = storeRepository.findWithinRadius(centerLat, centerLng, 3000.0)

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("가까운 베이커리")
    }

    @Test
    fun `반경 내 가게가 없으면 빈 리스트를 반환한다`() {
        // given
        val centerLat = 37.5665
        val centerLng = 126.9780
        storeJpaRepository.save(
            storeEntity("먼 베이커리", latitude = 38.0000, longitude = 127.5000),
        )

        // when
        val result = storeRepository.findWithinRadius(centerLat, centerLng, 3000.0)

        // then
        assertThat(result).isEmpty()
    }

    private fun storeEntity(
        name: String,
        latitude: Double,
        longitude: Double,
    ) = StoreJpaEntity(
        name = name,
        latitude = latitude,
        longitude = longitude,
        address = "서울시 중구 테스트로 1",
    )
}
