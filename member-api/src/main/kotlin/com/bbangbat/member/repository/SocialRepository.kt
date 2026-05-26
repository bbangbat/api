package com.bbangbat.member.repository

import com.bbangbat.member.domain.Social
import com.bbangbat.member.domain.SocialType
import org.springframework.stereotype.Repository

@Repository
class SocialRepository(
    private val socialJpaRepository: SocialJpaRepository,
    private val memberJpaRepository: MemberJpaRepository,
) {
    fun findByProviderAndProviderId(
        provider: SocialType,
        providerId: String,
    ): Social? =
        socialJpaRepository
            .findByProviderAndProviderId(provider, providerId)
            .orElse(null)
            ?.toDomain()

    fun save(social: Social): Social {
        val memberRef = memberJpaRepository.getReferenceById(social.member.id)

        return socialJpaRepository.save(SocialJpaEntity.from(social, memberRef)).toDomain()
    }
}
