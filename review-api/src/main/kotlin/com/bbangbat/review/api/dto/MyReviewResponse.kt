package com.bbangbat.review.api.dto

import com.bbangbat.review.application.MyReview
import java.time.LocalDateTime

data class MyReviewResponse(
    val id: Long,
    val storeId: Long,
    val storeName: String,
    val storeImageUrl: String,
    val rating: Int,
    val menus: List<String>,
    val content: String,
    val imageUrls: List<String>,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(myReview: MyReview): MyReviewResponse =
            MyReviewResponse(
                id = myReview.review.id,
                storeId = myReview.store.id,
                storeName = myReview.store.name,
                storeImageUrl = myReview.store.imageUrl,
                rating = myReview.review.rating,
                menus = myReview.review.menus,
                content = myReview.review.content,
                imageUrls = myReview.review.imageUrls,
                createdAt = myReview.review.createdAt,
            )
    }
}
