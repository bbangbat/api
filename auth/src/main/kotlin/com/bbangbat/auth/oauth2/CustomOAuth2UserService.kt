package com.bbangbat.auth.oauth2

import com.bbangbat.auth.OAuthMemberPort
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(
    private val oAuthMemberPort: OAuthMemberPort,
) : DefaultOAuth2UserService() {
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val userInfo =
            when (userRequest.clientRegistration.registrationId.uppercase()) {
                "NAVER" -> NaverOAuth2UserInfo(oAuth2User.attributes)
                "KAKAO" -> KakaoOAuth2UserInfo(oAuth2User.attributes)
                else -> throw IllegalArgumentException("지원하지 않는 소셜 로그인입니다.")
            }

        val memberId = oAuthMemberPort.findByProviderAndProviderId(userInfo.provider, userInfo.providerId)

        val attributes =
            mutableMapOf<String, Any>(
                "email" to userInfo.email,
                "name" to userInfo.name,
                "provider" to userInfo.provider,
                "providerId" to userInfo.providerId,
            )
        if (memberId != null) {
            attributes["memberId"] = memberId
        } else {
            attributes["existingAccount"] = oAuthMemberPort.existsByEmail(userInfo.email)
        }

        return DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_MEMBER")),
            attributes,
            "email",
        )
    }
}
