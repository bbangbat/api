package com.bbangbat.review.api.dto

import com.bbangbat.review.domain.Review
import java.time.LocalDateTime

data class ReviewResponse(
    val id: Long,
    val memberId: Long,
    val rating: Int,
    val menus: List<String>,
    val content: String,
    val imageUrls: List<String>,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(review: Review) =
            ReviewResponse(
                id = review.id,
                memberId = review.memberId,
                rating = review.rating,
                menus = review.menus,
                content = review.content,
                imageUrls = review.imageUrls,
                createdAt = review.createdAt,
            )
    }
}
