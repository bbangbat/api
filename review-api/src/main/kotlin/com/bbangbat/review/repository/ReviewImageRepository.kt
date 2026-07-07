package com.bbangbat.review.repository

import org.springframework.data.jpa.repository.JpaRepository

interface ReviewImageRepository : JpaRepository<ReviewImageJpaEntity, Long> {
    fun findAllByReviewInOrderByDisplayOrder(reviews: Collection<ReviewJpaEntity>): List<ReviewImageJpaEntity>

    fun deleteAllByReview(review: ReviewJpaEntity)
}
