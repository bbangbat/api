package com.bbangbat

import com.bbangbat.auth.oauth2.SocialProvider
import com.bbangbat.member.application.MemberService
import com.bbangbat.member.domain.SocialType
import com.bbangbat.member.repository.SocialPersistenceAdapter
import org.springframework.stereotype.Component
import com.bbangbat.auth.oauth2.MemberPort as AuthMemberPort
import com.bbangbat.live.application.MemberPort as LiveMemberPort

@Component
class MemberPortAdapter(
    private val memberService: MemberService,
    private val socialPersistenceAdapter: SocialPersistenceAdapter,
) : AuthMemberPort,
    LiveMemberPort {
    override fun getNickname(memberId: Long): String = memberService.findById(memberId).nickname

    override fun findByProviderAndProviderId(
        provider: SocialProvider,
        providerId: String,
    ): Long? = socialPersistenceAdapter.findByProviderAndProviderId(SocialType.valueOf(provider.name), providerId)?.member?.id

    override fun existsByEmail(email: String): Boolean = memberService.findByEmailOrNull(email) != null

    override fun updateLastLoginAt(memberId: Long) = memberService.updateLastLoginAt(memberId)
}
