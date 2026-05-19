package com.bbangbat.auth

import com.bbangbat.auth.oauth2.SocialProvider

interface OAuthMemberPort {
    fun findByProviderAndProviderId(
        provider: SocialProvider,
        providerId: String,
    ): Long?

    fun existsByEmail(email: String): Boolean

    fun updateLastLoginAt(memberId: Long)
}
