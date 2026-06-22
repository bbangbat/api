package com.bbangbat.review.repository

import org.springframework.data.jpa.repository.JpaRepository

interface ReviewJpaRepository : JpaRepository<ReviewJpaEntity, Long> {
    fun findAllByStoreIdOrderByIdDesc(storeId: Long): List<ReviewJpaEntity>
}
