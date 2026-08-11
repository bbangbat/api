package com.bbangbat.review.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository :
    JpaRepository<ReviewJpaEntity, Long>,
    KotlinJdslJpqlExecutor {
    fun findAllByStoreIdOrderByIdDesc(storeId: Long): List<ReviewJpaEntity>

    fun findAllByMemberIdOrderByIdDesc(memberId: Long): List<ReviewJpaEntity>

    fun countByMemberId(memberId: Long): Long
}
