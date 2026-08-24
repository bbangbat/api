package com.bbangbat.live.application

import com.bbangbat.auth.voter.Voter
import com.bbangbat.auth.voter.VoterType
import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.CONGESTION_VOTE_COOLDOWN
import com.bbangbat.common.exception.ErrorCode.CONGESTION_VOTE_TOO_FAR
import com.bbangbat.common.exception.ErrorCode.OUT_OF_SERVICE_AREA
import com.bbangbat.common.exception.ErrorCode.STORE_NOT_FOUND
import com.bbangbat.common.geo.GeoDistance
import com.bbangbat.live.domain.Congestion
import com.bbangbat.live.domain.CongestionLevel
import com.bbangbat.live.domain.CongestionVote
import com.bbangbat.live.domain.ServiceArea
import com.bbangbat.live.repository.CongestionVotePersistenceAdapter
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
class CongestionService(
    private val congestionVotePersistenceAdapter: CongestionVotePersistenceAdapter,
    private val storePort: StorePort,
    @param:Value("\${app.congestion.vote-max-distance-meters}") private val maxDistanceMeters: Double,
    @param:Value("\${app.congestion.vote-cooldown-minutes}") private val cooldownMinutes: Long,
) {
    @Transactional
    fun vote(
        storeId: Long,
        level: CongestionLevel,
        latitude: Double,
        longitude: Double,
        voter: Voter,
    ): Congestion {
        verifyLocation(storeId, latitude, longitude)

        val now = LocalDateTime.now()
        // 쿨다운 검사와 갱신 사이에 동시 요청이 끼어들지 못하도록 잠그고 조회한다.
        val existing = congestionVotePersistenceAdapter.findByVoterForUpdate(storeId, voter.type, voter.key)

        if (existing != null) {
            verifyCooldown(existing.votedAt, now)
            congestionVotePersistenceAdapter.update(existing.revote(level, now))
        } else {
            saveFirstVote(storeId, level, voter, now)
        }

        return getCongestion(storeId)
    }

    @Transactional(readOnly = true)
    fun getCongestion(storeId: Long): Congestion {
        val from = Congestion.windowStart(LocalDateTime.now())
        val votes = congestionVotePersistenceAdapter.findRecentVotes(storeId, from)

        return Congestion.summarizeVotes(storeId, votes)
    }

    @Transactional(readOnly = true)
    fun getCongestions(storeIds: List<Long>): Map<Long, Congestion> {
        if (storeIds.isEmpty()) {
            return emptyMap()
        }

        val from = Congestion.windowStart(LocalDateTime.now())
        val countsByStore =
            congestionVotePersistenceAdapter
                .countRecentVotesByStores(storeIds, from)
                .groupBy { it.storeId }

        return storeIds.associateWith { storeId ->
            val counts = countsByStore[storeId].orEmpty().associate { it.level to it.count.toInt() }

            Congestion.summarizeCounts(storeId, counts)
        }
    }

    /** 회원 탈퇴 시 해당 회원이 남긴 투표를 모두 제거한다. */
    @Transactional
    fun deleteVotesByMember(memberId: Long) {
        congestionVotePersistenceAdapter.deleteAllByVoter(VoterType.MEMBER, memberId.toString())
    }

    /**
     * 서비스 지역(대전) 안인지 확인하고, 가게와의 실제 거리가 허용 범위 내인지 검증한다.
     */
    private fun verifyLocation(
        storeId: Long,
        latitude: Double,
        longitude: Double,
    ) {
        if (!ServiceArea.contains(latitude, longitude)) {
            throw BbangbatException(OUT_OF_SERVICE_AREA)
        }

        val store = storePort.findCoordinates(storeId) ?: throw BbangbatException(STORE_NOT_FOUND)
        val distance = GeoDistance.meters(latitude, longitude, store.latitude, store.longitude)

        if (distance > maxDistanceMeters) {
            throw BbangbatException(CONGESTION_VOTE_TOO_FAR)
        }
    }

    /**
     * 마지막 투표로부터 쿨다운이 지나지 않았으면 남은 시간을 담아 거절한다.
     */
    private fun verifyCooldown(
        lastVotedAt: LocalDateTime,
        now: LocalDateTime,
    ) {
        val availableAt = lastVotedAt.plusMinutes(cooldownMinutes)

        if (now.isBefore(availableAt)) {
            val retryAfterSeconds = Duration.between(now, availableAt).seconds.coerceAtLeast(1)

            throw BbangbatException(CONGESTION_VOTE_COOLDOWN, retryAfterSeconds)
        }
    }

    /**
     * 첫 투표 저장. 동시 요청이 unique 제약에 걸리면 이미 방금 투표된 것이므로 쿨다운으로 처리한다.
     */
    private fun saveFirstVote(
        storeId: Long,
        level: CongestionLevel,
        voter: Voter,
        now: LocalDateTime,
    ) {
        try {
            congestionVotePersistenceAdapter.save(
                CongestionVote(
                    storeId = storeId,
                    level = level,
                    voterType = voter.type,
                    voterKey = voter.key,
                    votedAt = now,
                ),
            )
        } catch (e: DataIntegrityViolationException) {
            throw BbangbatException(CONGESTION_VOTE_COOLDOWN, Duration.ofMinutes(cooldownMinutes).seconds)
        }
    }
}
