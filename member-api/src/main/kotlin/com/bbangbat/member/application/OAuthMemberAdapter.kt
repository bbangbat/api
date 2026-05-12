package com.bbangbat.member.application

import com.bbangbat.auth.OAuthMemberPort
import com.bbangbat.auth.oauth2.SocialProvider
import com.bbangbat.member.domain.SocialType
import com.bbangbat.member.repository.SocialRepository
import org.springframework.stereotype.Component

@Component
class OAuthMemberAdapter(
    private val socialRepository: SocialRepository,
    private val memberService: MemberService,
) : OAuthMemberPort {
    override fun findByProviderAndProviderId(
        provider: SocialProvider,
        providerId: String,
    ): Long? = socialRepository.findByProviderAndProviderId(SocialType.valueOf(provider.name), providerId)?.member?.id

    override fun existsByEmail(email: String): Boolean = memberService.findByEmailOrNull(email) != null
}
