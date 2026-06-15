package com.bbangbat.live.congestion.domain

import com.bbangbat.auth.voter.VoterType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class CongestionTest {
    @Test
    fun `투표를 혼잡도별로 집계하고 최다 득표를 current로 설정한다`() {
        // given
        val votes =
            listOf(
                vote(CongestionLevel.UNCROWDED),
                vote(CongestionLevel.NORMAL),
                vote(CongestionLevel.NORMAL),
                vote(CongestionLevel.CROWDED),
                vote(CongestionLevel.CROWDED),
                vote(CongestionLevel.CROWDED),
            )

        // when
        val congestion = Congestion.of(storeId = 1L, votes = votes)

        // then
        assertThat(congestion.current).isEqualTo(CongestionLevel.CROWDED)
        assertThat(congestion.totalVotes).isEqualTo(6)
        assertThat(congestion.counts[CongestionLevel.UNCROWDED]).isEqualTo(1)
        assertThat(congestion.counts[CongestionLevel.NORMAL]).isEqualTo(2)
        assertThat(congestion.counts[CongestionLevel.CROWDED]).isEqualTo(3)
    }

    @Test
    fun `득표가 동점이면 더 혼잡한 쪽을 current로 설정한다`() {
        // given (NORMAL 2표, CROWDED 2표 동점)
        val votes =
            listOf(
                vote(CongestionLevel.NORMAL),
                vote(CongestionLevel.NORMAL),
                vote(CongestionLevel.CROWDED),
                vote(CongestionLevel.CROWDED),
            )

        // when
        val congestion = Congestion.of(storeId = 1L, votes = votes)

        // then
        assertThat(congestion.current).isEqualTo(CongestionLevel.CROWDED)
    }

    @Test
    fun `투표가 없으면 current는 기본값 UNCROWDED이고 모든 혼잡도 카운트는 0이다`() {
        // when
        val congestion = Congestion.of(storeId = 1L, votes = emptyList())

        // then
        assertThat(congestion.current).isEqualTo(CongestionLevel.UNCROWDED)
        assertThat(congestion.totalVotes).isEqualTo(0)
        assertThat(congestion.counts).containsValues(0, 0, 0)
    }

    private fun vote(level: CongestionLevel): CongestionVote =
        CongestionVote(
            storeId = 1L,
            level = level,
            voterType = VoterType.GUEST,
            voterKey = "guest-${level.name}-${System.nanoTime()}",
            votedAt = LocalDateTime.now(),
        )
}
