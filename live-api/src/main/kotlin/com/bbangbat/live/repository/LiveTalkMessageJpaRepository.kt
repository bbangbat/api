package com.bbangbat.live.repository

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface LiveTalkMessageJpaRepository : JpaRepository<LiveTalkMessageJpaEntity, Long> {
    fun findAllByStoreIdAndCreatedAtGreaterThanEqualOrderByIdAsc(
        storeId: Long,
        from: LocalDateTime,
    ): List<LiveTalkMessageJpaEntity>

    fun findAllByStoreIdAndIdGreaterThanAndCreatedAtGreaterThanEqualOrderByIdAsc(
        storeId: Long,
        afterId: Long,
        from: LocalDateTime,
    ): List<LiveTalkMessageJpaEntity>
}
