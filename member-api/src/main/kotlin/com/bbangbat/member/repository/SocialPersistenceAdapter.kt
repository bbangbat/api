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

    fun findAllByMemberId(memberId: Long): List<Social> = socialRepository.findAllByMemberId(memberId).map { it.toDomain() }

    fun deleteAllByMemberId(memberId: Long) = socialRepository.deleteAllByMemberId(memberId)

    fun delete(id: Long) = socialRepository.deleteById(id)

    fun save(social: Social): Social {
        val memberRef = memberRepository.getReferenceById(social.member.id)

        return socialRepository.save(SocialJpaEntity.from(social, memberRef)).toDomain()
    }
}
