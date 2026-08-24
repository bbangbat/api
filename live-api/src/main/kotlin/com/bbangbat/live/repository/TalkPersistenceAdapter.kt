package com.bbangbat.live.repository

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.NOT_FOUND
import com.bbangbat.common.exception.ErrorCode.TALK_NOT_FOUND
import com.bbangbat.live.domain.LiveTalkMessage
import com.bbangbat.live.domain.StoreTalkSummary
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
class TalkPersistenceAdapter(
    private val liveTalkMessageRepository: LiveTalkMessageRepository,
    private val storeTalkSummaryRepository: StoreTalkSummaryRepository,
) {
    fun saveMessage(message: LiveTalkMessage): LiveTalkMessage =
        liveTalkMessageRepository.save(LiveTalkMessageJpaEntity.from(message)).toDomain()

    fun countByAuthorId(authorId: Long): Long = liveTalkMessageRepository.countByAuthorIdAndDeletedAtIsNull(authorId)

    /** 내가 쓴 톡 목록 (최신순). 24시간 윈도우와 무관하게 전체를 돌려준다. */
    fun findByAuthorId(authorId: Long): List<LiveTalkMessage> =
        liveTalkMessageRepository.findAllByAuthorIdAndDeletedAtIsNullOrderByIdDesc(authorId).map { it.toDomain() }

    /** 삭제된 메시지도 그대로 돌려준다. 삭제 여부 판단은 도메인(isDeleted)의 몫이다. */
    fun findMessageById(id: Long): LiveTalkMessage? =
        liveTalkMessageRepository
            .findById(id)
            .orElse(null)
            ?.toDomain()

    /**
     * 변경된 도메인 상태를 영속 엔티티에 반영한다. (더티체킹)
     */
    @Transactional
    fun updateMessage(message: LiveTalkMessage): LiveTalkMessage =
        liveTalkMessageRepository
            .findById(message.id)
            .orElseThrow { BbangbatException(TALK_NOT_FOUND) }
            .also { it.applyFrom(message) }
            .toDomain()

    fun findRecentMessages(
        storeId: Long,
        from: LocalDateTime,
        afterId: Long?,
    ): List<LiveTalkMessage> {
        val entities: List<LiveTalkMessageJpaEntity?> =
            liveTalkMessageRepository.findAll {
                select(entity(LiveTalkMessageJpaEntity::class))
                    .from(entity(LiveTalkMessageJpaEntity::class))
                    .where(
                        and(
                            path(LiveTalkMessageJpaEntity::storeId).eq(value(storeId)),
                            path(LiveTalkMessageJpaEntity::createdAt).ge(value(from)),
                            path(LiveTalkMessageJpaEntity::deletedAt).isNull(),
                            afterId?.let { path(LiveTalkMessageJpaEntity::id).gt(value(it)) },
                        ),
                    ).orderBy(path(LiveTalkMessageJpaEntity::id).asc())
            }

        return entities.filterNotNull().map { it.toDomain() }
    }

    /**
     * 집계 윈도우(from 이후) 내 톡이 minCount 이상인 활성 가게와 각 가게의 최신 메시지 ID를 조회한다.
     * (group by having, 요약 대상 선정용)
     */
    fun findActiveStores(
        from: LocalDateTime,
        minCount: Long,
    ): List<ActiveStore> {
        val results: List<ActiveStore?> =
            liveTalkMessageRepository.findAll {
                selectNew<ActiveStore>(
                    path(LiveTalkMessageJpaEntity::storeId),
                    max(path(LiveTalkMessageJpaEntity::id)),
                ).from(
                    entity(LiveTalkMessageJpaEntity::class),
                ).where(
                    and(
                        path(LiveTalkMessageJpaEntity::createdAt).ge(value(from)),
                        path(LiveTalkMessageJpaEntity::deletedAt).isNull(),
                    ),
                ).groupBy(
                    path(LiveTalkMessageJpaEntity::storeId),
                ).having(
                    count(path(LiveTalkMessageJpaEntity::id)).ge(value(minCount)),
                )
            }

        return results.filterNotNull()
    }

    fun saveSummary(summary: StoreTalkSummary): StoreTalkSummary =
        storeTalkSummaryRepository.save(StoreTalkSummaryJpaEntity.from(summary)).toDomain()

    /**
     * 변경된 도메인 상태를 영속 엔티티에 반영한다. (더티체킹)
     */
    @Transactional
    fun updateSummary(summary: StoreTalkSummary): StoreTalkSummary =
        storeTalkSummaryRepository
            .findById(summary.id)
            .orElseThrow { BbangbatException(NOT_FOUND) }
            .also { it.applyFrom(summary) }
            .toDomain()

    fun findSummaryByStoreId(storeId: Long): StoreTalkSummary? = storeTalkSummaryRepository.findByStoreId(storeId).orElse(null)?.toDomain()

    fun findSummariesByStoreIds(storeIds: List<Long>): List<StoreTalkSummary> =
        storeTalkSummaryRepository.findAllByStoreIdIn(storeIds).map { it.toDomain() }
}
