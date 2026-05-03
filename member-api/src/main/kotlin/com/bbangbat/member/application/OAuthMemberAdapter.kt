package com.bbangbat.member.application

import com.bbangbat.auth.OAuthMemberPort
import org.springframework.stereotype.Component

@Component
class OAuthMemberAdapter(
    private val memberService: MemberService,
) : OAuthMemberPort {
    override fun find(
        email: String,
        name: String,
    ): Long {
        val member = memberService.findByEmail(email)

        return member.id
    }
}
