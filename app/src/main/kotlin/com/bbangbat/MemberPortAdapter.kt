package com.bbangbat

import com.bbangbat.live.application.MemberPort
import com.bbangbat.member.application.MemberService
import org.springframework.stereotype.Component

@Component
class MemberPortAdapter(
    private val memberService: MemberService,
) : MemberPort {
    override fun getNickname(memberId: Long): String = memberService.findById(memberId).nickname
}
