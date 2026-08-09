package com.bbangbat.review.application

import com.bbangbat.review.domain.Review

/** 작성자 정보를 함께 담은 리뷰. 탈퇴한 회원의 리뷰는 WITHDRAWN_NICKNAME으로 표시한다. */
data class AuthoredReview(
    val review: Review,
    val authorNickname: String,
    val authorProfileImageUrl: String?,
) {
    companion object {
        const val WITHDRAWN_NICKNAME = "탈퇴한 회원"

        fun of(
            review: Review,
            author: ReviewAuthor?,
        ): AuthoredReview =
            AuthoredReview(
                review = review,
                authorNickname = author?.nickname ?: WITHDRAWN_NICKNAME,
                authorProfileImageUrl = author?.profileImageUrl,
            )
    }
}
