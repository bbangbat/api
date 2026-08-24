package com.bbangbat.live.repository

import com.bbangbat.auth.voter.VoterType
import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.NOT_FOUND
import com.bbangbat.live.domain.CongestionVote
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class CongestionVotePersistenceAdapter(
    private val congestionVoteRepository: CongestionVoteRepository,
) {
    fun findByVoter(
        storeId: Long,
        voterType: VoterType,
        voterKey: String,
    ): CongestionVote? =
        congestionVoteRepository
            .findByStoreIdAndVoterTypeAndVoterKey(storeId, voterType, voterKey)
            .orElse(null)
            ?.toDomain()

    /**
     * 쿨다운 검사와 갱신이 원자적으로 처리되도록 투표 행을 잠그고 조회한다.
     */
    fun findByVoterForUpdate(
        storeId: Long,
        voterType: VoterType,
        voterKey: String,
    ): CongestionVote? =
        congestionVoteRepository
            .findWithLockByStoreIdAndVoterTypeAndVoterKey(storeId, voterType, voterKey)
            .orElse(null)
            ?.toDomain()

    fun deleteAllByVoter(
        voterType: VoterType,
        voterKey: String,
    ) = congestionVoteRepository.deleteAllByVoterTypeAndVoterKey(voterType, voterKey)

    fun save(vote: CongestionVote): CongestionVote = congestionVoteRepository.save(CongestionVoteJpaEntity.from(vote)).toDomain()

    /**
     * 변경된 도메인 상태를 영속 엔티티에 반영한다. (더티체킹)
     */
    fun update(vote: CongestionVote): CongestionVote =
        congestionVoteRepository
            .findById(vote.id)
            .orElseThrow { BbangbatException(NOT_FOUND) }
            .also { it.applyFrom(vote) }
            .toDomain()

    fun findRecentVotes(
        storeId: Long,
        from: LocalDateTime,
    ): List<CongestionVote> =
        congestionVoteRepository
            .findAllByStoreIdAndVotedAtGreaterThanEqual(storeId, from)
            .map { it.toDomain() }

    /**
     * 여러 가게의 최근 투표를 가게별·혼잡도별로 한 번에 집계한다. (IN + group by)
     */
    fun countRecentVotesByStores(
        storeIds: List<Long>,
        from: LocalDateTime,
    ): List<StoreCongestionCount> {
        val counts: List<StoreCongestionCount?> =
            congestionVoteRepository.findAll {
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
            }

        return counts.filterNotNull()
    }
}
