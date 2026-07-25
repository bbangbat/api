package com.bbangbat

import com.bbangbat.member.application.ReviewPort
import com.bbangbat.review.application.ReviewService
import org.springframework.stereotype.Component

@Component
class ReviewPortAdapter(
    private val reviewService: ReviewService,
) : ReviewPort {
    override fun countByMemberId(memberId: Long): Long = reviewService.countByMemberId(memberId)
}
