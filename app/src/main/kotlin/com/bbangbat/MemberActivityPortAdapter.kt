package com.bbangbat

import com.bbangbat.live.application.TalkService
import com.bbangbat.member.application.MemberActivityPort
import com.bbangbat.review.application.ReviewService
import org.springframework.stereotype.Component

@Component
class MemberActivityPortAdapter(
    private val reviewService: ReviewService,
    private val talkService: TalkService,
) : MemberActivityPort {
    override fun countReviews(memberId: Long): Long = reviewService.countByMemberId(memberId)

    override fun countTalks(memberId: Long): Long = talkService.countByAuthorId(memberId)
}
