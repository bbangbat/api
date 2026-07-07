package com.bbangbat.member.application

import com.bbangbat.auth.oauth2.OAuthMemberPort
import com.bbangbat.auth.oauth2.SocialProvider
import com.bbangbat.member.domain.SocialType
import com.bbangbat.member.repository.SocialPersistenceAdapter
import org.springframework.stereotype.Component

@Component
class OAuthMemberAdapter(
    private val socialPersistenceAdapter: SocialPersistenceAdapter,
    private val memberService: MemberService,
) : OAuthMemberPort {
    override fun findByProviderAndProviderId(
        provider: SocialProvider,
        providerId: String,
    ): Long? = socialPersistenceAdapter.findByProviderAndProviderId(SocialType.valueOf(provider.name), providerId)?.member?.id

    override fun existsByEmail(email: String): Boolean = memberService.findByEmailOrNull(email) != null

    override fun updateLastLoginAt(memberId: Long) = memberService.updateLastLoginAt(memberId)
}
