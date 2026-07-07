package com.bbangbat.member.repository

import com.bbangbat.member.domain.SocialType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface SocialRepository : JpaRepository<SocialJpaEntity, Long> {
    fun findByProviderAndProviderId(
        provider: SocialType,
        providerId: String,
    ): Optional<SocialJpaEntity>
}
