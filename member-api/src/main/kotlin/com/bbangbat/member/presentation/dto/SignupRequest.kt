package com.bbangbat.member.presentation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원가입 요청")
data class SignupRequest(
    @field:Schema(description = "임시 토큰 (소셜 로그인 후 발급)") val tempToken: String,
    @field:Schema(description = "닉네임 (2~20자)", example = "빵괴물") val nickname: String,
    @field:Schema(description = "프로필 이미지 URL", example = "https://example.com/image.jpg")
    val profileImageUrl: String? = null,
)
