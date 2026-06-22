package com.bbangbat.member.application

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.FAVORITE_ALREADY_EXISTS
import com.bbangbat.common.exception.ErrorCode.FAVORITE_NOT_FOUND
import com.bbangbat.member.domain.Favorite
import com.bbangbat.member.repository.FavoriteRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class FavoriteServiceTest {
    @Mock
    private lateinit var favoriteRepository: FavoriteRepository

    private lateinit var favoriteService: FavoriteService

    @BeforeEach
    fun setUp() {
        favoriteService = FavoriteService(favoriteRepository)
    }

    @Test
    fun `즐겨찾기를 추가할 수 있다`() {
        // given
        val memberId = 1L
        val storeId = 10L
        given(favoriteRepository.existsByMemberIdAndStoreId(memberId, storeId)).willReturn(false)

        // when
        favoriteService.add(memberId, storeId)

        // then
        then(favoriteRepository).should().save(Favorite(memberId = memberId, storeId = storeId))
    }

    @Test
    fun `이미 즐겨찾기한 가게를 추가하면 예외를 던진다`() {
        // given
        val memberId = 1L
        val storeId = 10L
        given(favoriteRepository.existsByMemberIdAndStoreId(memberId, storeId)).willReturn(true)

        // when & then
        val exception = assertThrows<BbangbatException> { favoriteService.add(memberId, storeId) }
        assertThat(exception.errorCode).isEqualTo(FAVORITE_ALREADY_EXISTS)
        then(favoriteRepository).shouldHaveNoMoreInteractions()
    }

    @Test
    fun `즐겨찾기를 삭제할 수 있다`() {
        // given
        val memberId = 1L
        val storeId = 10L
        val favorite = Favorite(id = 100L, memberId = memberId, storeId = storeId)
        given(favoriteRepository.findByMemberIdAndStoreId(memberId, storeId)).willReturn(favorite)

        // when
        favoriteService.remove(memberId, storeId)

        // then
        then(favoriteRepository).should().delete(favorite)
    }

    @Test
    fun `즐겨찾기하지 않은 가게를 삭제하면 예외를 던진다`() {
        // given
        val memberId = 1L
        val storeId = 10L
        given(favoriteRepository.findByMemberIdAndStoreId(memberId, storeId)).willReturn(null)

        // when & then
        val exception = assertThrows<BbangbatException> { favoriteService.remove(memberId, storeId) }
        assertThat(exception.errorCode).isEqualTo(FAVORITE_NOT_FOUND)
    }

    @Test
    fun `즐겨찾기 목록을 storeId 리스트로 반환한다`() {
        // given
        val memberId = 1L
        given(favoriteRepository.findAllStoreIdsByMemberId(memberId)).willReturn(listOf(10L, 20L, 30L))

        // when
        val result = favoriteService.findStoreIds(memberId)

        // then
        assertThat(result).containsExactly(10L, 20L, 30L)
    }

    @Test
    fun `즐겨찾기가 없으면 빈 리스트를 반환한다`() {
        // given
        val memberId = 1L
        given(favoriteRepository.findAllStoreIdsByMemberId(memberId)).willReturn(emptyList())

        // when
        val result = favoriteService.findStoreIds(memberId)

        // then
        assertThat(result).isEmpty()
    }
}
