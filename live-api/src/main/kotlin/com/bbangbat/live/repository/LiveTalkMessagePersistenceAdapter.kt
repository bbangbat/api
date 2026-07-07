package com.bbangbat.live.repository

import com.bbangbat.live.domain.LiveTalkMessage
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class LiveTalkMessagePersistenceAdapter(
    private val liveTalkMessageRepository: LiveTalkMessageRepository,
) {
    fun save(message: LiveTalkMessage): LiveTalkMessage = liveTalkMessageRepository.save(LiveTalkMessageJpaEntity.from(message)).toDomain()

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
}
