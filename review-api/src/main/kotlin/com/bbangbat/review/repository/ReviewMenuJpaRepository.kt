package com.bbangbat.review.repository

import org.springframework.data.jpa.repository.JpaRepository

interface ReviewMenuJpaRepository : JpaRepository<ReviewMenuJpaEntity, Long> {
    fun findAllByReviewIn(reviews: Collection<ReviewJpaEntity>): List<ReviewMenuJpaEntity>

    fun deleteAllByReview(review: ReviewJpaEntity)
}
