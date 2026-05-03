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
        // 소셜 서버로부터 사용자 정보 가져오기
        val oAuth2User = super.loadUser(userRequest)
        val provider = userRequest.clientRegistration.registrationId.uppercase()

        // 회원 정보 추출
        val userInfo =
            when (provider) {
                "NAVER" -> NaverOAuth2UserInfo(oAuth2User.attributes)
                "KAKAO" -> KakaoOAuth2UserInfo(oAuth2User.attributes)
                else -> throw IllegalArgumentException("지원하지 않는 소셜 로그인입니다.")
            }

        // member-api 서버에서 회원 조회
        val memberId = oAuthMemberPort.find(userInfo.email, userInfo.name)

        // memberId 담은 인증 객체 리턴
        return DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_MEMBER")),
            mapOf("memberId" to memberId),
            "memberId",
        )
    }
}
