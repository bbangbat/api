package com.bbangbat.live.repository

import com.bbangbat.live.domain.LiveTalkMessage
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class LiveTalkMessageRepository(
    private val liveTalkMessageJpaRepository: LiveTalkMessageJpaRepository,
    private val jdslExecutor: KotlinJdslJpqlExecutor,
) {
    fun save(message: LiveTalkMessage): LiveTalkMessage =
        liveTalkMessageJpaRepository.save(LiveTalkMessageJpaEntity.from(message)).toDomain()

    fun findRecentMessages(
        storeId: Long,
        from: LocalDateTime,
        afterId: Long?,
    ): List<LiveTalkMessage> =
        jdslExecutor
            .findAll {
                select(entity(LiveTalkMessageJpaEntity::class))
                    .from(entity(LiveTalkMessageJpaEntity::class))
                    .where(
                        and(
                            path(LiveTalkMessageJpaEntity::storeId).eq(value(storeId)),
                            path(LiveTalkMessageJpaEntity::createdAt).ge(value(from)),
                            afterId?.let { path(LiveTalkMessageJpaEntity::id).gt(value(it)) },
                        ),
                    ).orderBy(path(LiveTalkMessageJpaEntity::id).asc())
            }.filterNotNull()
            .map { it.toDomain() }
}
