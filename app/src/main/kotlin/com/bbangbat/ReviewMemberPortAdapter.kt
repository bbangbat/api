package com.bbangbat

import com.bbangbat.member.application.MemberService
import com.bbangbat.member.repository.MemberPersistenceAdapter
import com.bbangbat.review.application.MemberPort
import com.bbangbat.review.application.ReviewAuthor
import org.springframework.stereotype.Component

@Component
class ReviewMemberPortAdapter(
    private val memberPersistenceAdapter: MemberPersistenceAdapter,
    private val memberService: MemberService,
) : MemberPort {
    override fun findAuthors(memberIds: Collection<Long>): Map<Long, ReviewAuthor> =
        memberPersistenceAdapter.findAllByIds(memberIds).associate { member ->
            member.id to
                ReviewAuthor(
                    nickname = member.nickname,
                    profileImageUrl = memberService.profileImageUrlOf(member),
                )
        }
}
