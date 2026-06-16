package com.bbangbat.live.repository

import com.bbangbat.auth.voter.VoterType
import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.NOT_FOUND
import com.bbangbat.live.domain.CongestionLevel
import com.bbangbat.live.domain.CongestionVote
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class CongestionVoteRepository(
    private val congestionVoteJpaRepository: CongestionVoteJpaRepository,
    private val jdslExecutor: KotlinJdslJpqlExecutor,
) {
    fun findByVoter(
        storeId: Long,
        voterType: VoterType,
        voterKey: String,
    ): CongestionVote? =
        congestionVoteJpaRepository
            .findByStoreIdAndVoterTypeAndVoterKey(storeId, voterType, voterKey)
            .orElse(null)
            ?.toDomain()

    fun save(vote: CongestionVote): CongestionVote = congestionVoteJpaRepository.save(CongestionVoteJpaEntity.from(vote)).toDomain()

    fun updateVote(
        id: Long,
        level: CongestionLevel,
        votedAt: LocalDateTime,
    ) {
        congestionVoteJpaRepository
            .findById(id)
            .orElseThrow { BbangbatException(NOT_FOUND) }
            .also {
                it.level = level
                it.votedAt = votedAt
            }
    }

    fun findRecentVotes(
        storeId: Long,
        from: LocalDateTime,
    ): List<CongestionVote> =
        congestionVoteJpaRepository
            .findAllByStoreIdAndVotedAtGreaterThanEqual(storeId, from)
            .map { it.toDomain() }

    /**
     * 여러 가게의 최근 투표를 가게별·혼잡도별로 한 번에 집계한다. (IN + group by)
     */
    fun countRecentVotesByStores(
        storeIds: List<Long>,
        from: LocalDateTime,
    ): List<StoreCongestionCount> =
        jdslExecutor
            .findAll {
                selectNew<StoreCongestionCount>(
                    path(CongestionVoteJpaEntity::storeId),
                    path(CongestionVoteJpaEntity::level),
                    count(path(CongestionVoteJpaEntity::id)),
                ).from(
                    entity(CongestionVoteJpaEntity::class),
                ).where(
                    and(
                        path(CongestionVoteJpaEntity::storeId).`in`(storeIds),
                        path(CongestionVoteJpaEntity::votedAt).ge(value(from)),
                    ),
                ).groupBy(
                    path(CongestionVoteJpaEntity::storeId),
                    path(CongestionVoteJpaEntity::level),
                )
            }.filterNotNull()
}
