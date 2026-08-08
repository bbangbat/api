package com.bbangbat.auth.oauth2

import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error

/** 소셜 응답에 필수 사용자 정보가 없을 때 사용하는 에러 코드 */
const val MISSING_USER_ATTRIBUTE_ERROR = "missing_user_attribute"

/**
 * 소셜 응답에서 필수 값을 안전하게 꺼낸다.
 * 값이 없거나 타입이 다르면 강제 캐스팅 예외 대신 제어된 OAuth2AuthenticationException으로 변환한다.
 * (메시지에는 값이 아닌 키 이름만 담아 개인정보를 남기지 않는다)
 */
private fun missingAttribute(key: String): OAuth2AuthenticationException =
    OAuth2AuthenticationException(
        OAuth2Error(MISSING_USER_ATTRIBUTE_ERROR, "소셜 계정에서 필수 정보를 가져오지 못했습니다: $key", null),
        "소셜 계정에서 필수 정보를 가져오지 못했습니다: $key",
    )

@Suppress("UNCHECKED_CAST")
private fun Map<String, Any>.requiredMap(key: String): Map<String, Any> = this[key] as? Map<String, Any> ?: throw missingAttribute(key)

private fun Map<String, Any>.requiredString(key: String): String =
    (this[key] as? String)?.takeIf { it.isNotBlank() } ?: throw missingAttribute(key)

private fun Map<String, Any>.requiredId(key: String): String =
    this[key]?.toString()?.takeIf { it.isNotBlank() } ?: throw missingAttribute(key)

abstract class OAuth2UserInfo(
    val attributes: Map<String, Any>,
) {
    abstract val email: String
    abstract val name: String
    abstract val provider: SocialProvider
    abstract val providerId: String
    abstract val ageGroup: String?

    /** MALE / FEMALE. 동의하지 않았거나 제공하지 않으면 null */
    abstract val gender: String?
}

class NaverOAuth2UserInfo(
    attributes: Map<String, Any>,
) : OAuth2UserInfo(attributes) {
    private val response = attributes.requiredMap("response")
    override val email: String get() = response.requiredString("email")
    override val name: String get() = response.requiredString("name")
    override val provider = SocialProvider.NAVER
    override val providerId: String get() = response.requiredId("id")
    override val ageGroup: String?
        get() =
            when (response["age"] as? String) {
                "10-19" -> "TEENS"
                "20-29" -> "TWENTIES"
                "30-39" -> "THIRTIES"
                "40-49" -> "FORTIES"
                "50-59" -> "FIFTIES"
                "60-" -> "SIXTIES_PLUS"
                else -> null
            }
    override val gender: String?
        get() =
            when (response["gender"] as? String) {
                "M" -> "MALE"
                "F" -> "FEMALE"
                else -> null
            }
}

class KakaoOAuth2UserInfo(
    attributes: Map<String, Any>,
) : OAuth2UserInfo(attributes) {
    private val kakaoAccount = attributes.requiredMap("kakao_account")
    private val profile = kakaoAccount.requiredMap("profile")

    // 카카오는 이메일이 선택 동의 항목이라 미동의 계정이면 값이 비어 있을 수 있다.
    override val email: String get() = kakaoAccount.requiredString("email")
    override val name: String get() = profile.requiredString("nickname")
    override val provider = SocialProvider.KAKAO
    override val providerId: String get() = attributes.requiredId("id")
    override val ageGroup: String?
        get() =
            when (kakaoAccount["age_range"] as? String) {
                "10-14", "15-19" -> "TEENS"
                "20-29" -> "TWENTIES"
                "30-39" -> "THIRTIES"
                "40-49" -> "FORTIES"
                "50-59" -> "FIFTIES"
                "60-69", "70-79", "80-89", "90-" -> "SIXTIES_PLUS"
                else -> null
            }
    override val gender: String?
        get() =
            when (kakaoAccount["gender"] as? String) {
                "male" -> "MALE"
                "female" -> "FEMALE"
                else -> null
            }
}
