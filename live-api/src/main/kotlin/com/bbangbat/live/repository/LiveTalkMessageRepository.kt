package com.bbangbat.live.repository

import com.bbangbat.live.domain.LiveTalkMessage
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class LiveTalkMessageRepository(
    private val liveTalkMessageJpaRepository: LiveTalkMessageJpaRepository,
) {
    fun save(message: LiveTalkMessage): LiveTalkMessage =
        liveTalkMessageJpaRepository.save(LiveTalkMessageJpaEntity.from(message)).toDomain()

    fun findRecentMessages(
        storeId: Long,
        from: LocalDateTime,
        afterId: Long?,
    ): List<LiveTalkMessage> =
        if (afterId == null) {
            liveTalkMessageJpaRepository.findAllByStoreIdAndCreatedAtGreaterThanEqualOrderByIdAsc(storeId, from)
        } else {
            liveTalkMessageJpaRepository.findAllByStoreIdAndIdGreaterThanAndCreatedAtGreaterThanEqualOrderByIdAsc(storeId, afterId, from)
        }.map { it.toDomain() }
}
