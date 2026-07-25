package com.bbangbat.member.application

import com.bbangbat.member.repository.FavoritePersistenceAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class MemberStatsServiceTest {
    @Mock
    private lateinit var favoritePersistenceAdapter: FavoritePersistenceAdapter

    @Mock
    private lateinit var memberActivityPort: MemberActivityPort

    private lateinit var memberStatsService: MemberStatsService

    @BeforeEach
    fun setUp() {
        memberStatsService = MemberStatsService(favoritePersistenceAdapter, memberActivityPort)
    }

    @Test
    fun `회원의 리뷰 즐겨찾기 톡 수를 집계한다`() {
        val memberId = 1L
        given(memberActivityPort.countReviews(memberId)).willReturn(3L)
        given(favoritePersistenceAdapter.countByMemberId(memberId)).willReturn(5L)
        given(memberActivityPort.countTalks(memberId)).willReturn(7L)

        val result = memberStatsService.getStats(memberId)

        assertThat(result.reviewCount).isEqualTo(3L)
        assertThat(result.favoriteCount).isEqualTo(5L)
        assertThat(result.talkCount).isEqualTo(7L)
    }
}
