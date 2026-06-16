package com.bbangbat.live.application

import com.bbangbat.auth.voter.Voter
import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.OUT_OF_SERVICE_AREA
import com.bbangbat.live.domain.Congestion
import com.bbangbat.live.domain.CongestionLevel
import com.bbangbat.live.domain.CongestionVote
import com.bbangbat.live.domain.ServiceArea
import com.bbangbat.live.repository.CongestionVoteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CongestionService(
    private val congestionVoteRepository: CongestionVoteRepository,
) {
    @Transactional
    fun vote(
        storeId: Long,
        level: CongestionLevel,
        latitude: Double,
        longitude: Double,
        voter: Voter,
    ): Congestion {
        if (!ServiceArea.contains(latitude, longitude)) {
            throw BbangbatException(OUT_OF_SERVICE_AREA)
        }

        val now = LocalDateTime.now()
        val existing = congestionVoteRepository.findByVoter(storeId, voter.type, voter.key)

        if (existing != null) {
            congestionVoteRepository.updateVote(existing.id, level, now)
        } else {
            congestionVoteRepository.save(
                CongestionVote(
                    storeId = storeId,
                    level = level,
                    voterType = voter.type,
                    voterKey = voter.key,
                    votedAt = now,
                ),
            )
        }

        return getCongestion(storeId)
    }

    @Transactional(readOnly = true)
    fun getCongestion(storeId: Long): Congestion {
        val from = LocalDateTime.now().minusMinutes(WINDOW_MINUTES)
        val votes = congestionVoteRepository.findRecentVotes(storeId, from)

        return Congestion.summarizeVotes(storeId, votes)
    }

    @Transactional(readOnly = true)
    fun getCongestions(storeIds: List<Long>): Map<Long, Congestion> {
        if (storeIds.isEmpty()) {
            return emptyMap()
        }

        val from = LocalDateTime.now().minusMinutes(WINDOW_MINUTES)
        val countsByStore =
            congestionVoteRepository
                .countRecentVotesByStores(storeIds, from)
                .groupBy { it.storeId }

        return storeIds.associateWith { storeId ->
            val counts = countsByStore[storeId].orEmpty().associate { it.level to it.count.toInt() }

            Congestion.summarizeCounts(storeId, counts)
        }
    }

    companion object {
        private const val WINDOW_MINUTES = 15L
    }
}
