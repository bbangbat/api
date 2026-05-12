package com.bbangbat.auth.oauth2

abstract class OAuth2UserInfo(
    val attributes: Map<String, Any>,
) {
    abstract val email: String
    abstract val name: String
    abstract val provider: SocialProvider
    abstract val providerId: String
}

class NaverOAuth2UserInfo(
    attributes: Map<String, Any>,
) : OAuth2UserInfo(attributes) {
    @Suppress("UNCHECKED_CAST")
    private val response = attributes["response"] as Map<String, Any>
    override val email: String get() = response["email"] as String
    override val name: String get() = response["name"] as String
    override val provider = SocialProvider.NAVER
    override val providerId: String get() = response["id"] as String
}

class KakaoOAuth2UserInfo(
    attributes: Map<String, Any>,
) : OAuth2UserInfo(attributes) {
    @Suppress("UNCHECKED_CAST")
    private val kakaoAccount = attributes["kakao_account"] as Map<String, Any>

    @Suppress("UNCHECKED_CAST")
    private val profile = kakaoAccount["profile"] as Map<String, Any>
    override val email: String get() = kakaoAccount["email"] as String
    override val name: String get() = profile["nickname"] as String
    override val provider = SocialProvider.KAKAO
    override val providerId: String get() = attributes["id"].toString()
}
