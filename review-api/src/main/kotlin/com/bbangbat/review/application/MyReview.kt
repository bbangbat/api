package com.bbangbat.review.application

import com.bbangbat.review.domain.Review

data class MyReview(
    val review: Review,
    val store: ReviewStore,
)
