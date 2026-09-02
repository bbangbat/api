package com.bbangbat.live.repository

import com.bbangbat.auth.voter.VoterType
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.time.LocalDateTime
import java.util.Optional

interface CongestionVoteRepository :
    JpaRepository<CongestionVoteJpaEntity, Long>,
    KotlinJdslJpqlExecutor {
    fun findByStoreIdAndVoterTypeAndVoterKey(
        storeId: Long,
        voterType: VoterType,
        voterKey: String,
    ): Optional<CongestionVoteJpaEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithLockByStoreIdAndVoterTypeAndVoterKey(
        storeId: Long,
        voterType: VoterType,
        voterKey: String,
    ): Optional<CongestionVoteJpaEntity>

    fun deleteAllByVoterTypeAndVoterKey(
        voterType: VoterType,
        voterKey: String,
    )

    fun findAllByStoreIdAndVotedAtGreaterThanEqual(
        storeId: Long,
        votedAt: LocalDateTime,
    ): List<CongestionVoteJpaEntity>
}
