package com.bbangbat.member.repository

import com.bbangbat.member.domain.Social
import com.bbangbat.member.domain.SocialType
import org.springframework.stereotype.Repository

@Repository
class SocialPersistenceAdapter(
    private val socialRepository: SocialRepository,
    private val memberRepository: MemberRepository,
) {
    fun findByProviderAndProviderId(
        provider: SocialType,
        providerId: String,
    ): Social? =
        socialRepository
            .findByProviderAndProviderId(provider, providerId)
            .orElse(null)
            ?.toDomain()

    fun save(social: Social): Social {
        val memberRef = memberRepository.getReferenceById(social.member.id)

        return socialRepository.save(SocialJpaEntity.from(social, memberRef)).toDomain()
    }
}
