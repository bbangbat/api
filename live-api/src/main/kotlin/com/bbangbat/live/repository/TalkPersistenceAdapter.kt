package com.bbangbat.live.repository

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

    fun countByAuthorId(authorId: Long): Long = liveTalkMessageRepository.countByAuthorId(authorId)

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
                    path(LiveTalkMessageJpaEntity::createdAt).ge(value(from)),
                ).groupBy(
                    path(LiveTalkMessageJpaEntity::storeId),
                ).having(
                    count(path(LiveTalkMessageJpaEntity::id)).ge(value(minCount)),
                )
            }

        return results.filterNotNull()
    }

    @Transactional
    fun upsertSummary(summary: StoreTalkSummary): StoreTalkSummary {
        val existing = storeTalkSummaryRepository.findByStoreId(summary.storeId).orElse(null)

        if (existing != null) {
            existing.summary = summary.summary
            existing.lastMessageId = summary.lastMessageId

            return existing.toDomain()
        }

        return storeTalkSummaryRepository.save(StoreTalkSummaryJpaEntity.from(summary)).toDomain()
    }

    fun findSummaryByStoreId(storeId: Long): StoreTalkSummary? = storeTalkSummaryRepository.findByStoreId(storeId).orElse(null)?.toDomain()

    fun findSummariesByStoreIds(storeIds: List<Long>): List<StoreTalkSummary> =
        storeTalkSummaryRepository.findAllByStoreIdIn(storeIds).map { it.toDomain() }
}
