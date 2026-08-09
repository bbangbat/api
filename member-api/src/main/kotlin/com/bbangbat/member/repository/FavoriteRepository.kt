package com.bbangbat.member.repository

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface FavoriteRepository : JpaRepository<FavoriteJpaEntity, Long> {
    fun existsByMemberIdAndStoreId(
        memberId: Long,
        storeId: Long,
    ): Boolean

    fun findByMemberIdAndStoreId(
        memberId: Long,
        storeId: Long,
    ): Optional<FavoriteJpaEntity>

    /** 최근에 추가한 즐겨찾기부터 반환한다. */
    fun findAllByMemberIdOrderByIdDesc(memberId: Long): List<FavoriteJpaEntity>

    fun countByMemberId(memberId: Long): Long

    fun deleteAllByMemberId(memberId: Long)
}
