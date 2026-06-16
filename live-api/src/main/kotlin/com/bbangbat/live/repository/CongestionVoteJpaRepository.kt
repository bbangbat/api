package com.bbangbat.live.repository

import com.bbangbat.auth.voter.VoterType
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.Optional

interface CongestionVoteJpaRepository : JpaRepository<CongestionVoteJpaEntity, Long> {
    fun findByStoreIdAndVoterTypeAndVoterKey(
        storeId: Long,
        voterType: VoterType,
        voterKey: String,
    ): Optional<CongestionVoteJpaEntity>

    fun findAllByStoreIdAndVotedAtGreaterThanEqual(
        storeId: Long,
        votedAt: LocalDateTime,
    ): List<CongestionVoteJpaEntity>
}
