package com.bbangbat.review.repository

import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository : JpaRepository<ReviewJpaEntity, Long> {
    fun findAllByStoreIdOrderByIdDesc(storeId: Long): List<ReviewJpaEntity>

    fun findAllByMemberIdOrderByIdDesc(memberId: Long): List<ReviewJpaEntity>

    fun countByMemberId(memberId: Long): Long
}
