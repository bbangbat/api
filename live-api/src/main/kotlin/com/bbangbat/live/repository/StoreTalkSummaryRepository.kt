package com.bbangbat.live.repository

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface StoreTalkSummaryRepository : JpaRepository<StoreTalkSummaryJpaEntity, Long> {
    fun findByStoreId(storeId: Long): Optional<StoreTalkSummaryJpaEntity>

    fun findAllByStoreIdIn(storeIds: Collection<Long>): List<StoreTalkSummaryJpaEntity>
}
