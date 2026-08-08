package com.bbangbat.member.api.dto

import com.bbangbat.member.domain.AgeGroup
import com.bbangbat.member.domain.Gender
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "회원가입 요청")
data class SignupRequest(
    @field:NotBlank(message = "임시 토큰이 필요합니다.")
    @field:Schema(description = "임시 토큰 (소셜 로그인 후 발급)")
    val tempToken: String,
    @field:NotBlank(message = "닉네임을 입력해주세요.")
    @field:Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    @field:Schema(description = "닉네임 (2~20자)", example = "빵괴물")
    val nickname: String,
    @field:Schema(description = "프로필 이미지 URL", example = "https://example.com/image.jpg")
    val profileImageKey: String? = null,
    @field:Schema(description = "성별")
    val gender: Gender,
    @field:Schema(description = "연령대")
    val ageGroup: AgeGroup,
    @field:AssertTrue(message = "서비스 이용약관에 동의해야 합니다.")
    @field:Schema(description = "서비스 이용약관 동의")
    val termsAgreed: Boolean,
    @field:AssertTrue(message = "개인정보처리방침에 동의해야 합니다.")
    @field:Schema(description = "개인정보처리방침 동의")
    val privacyAgreed: Boolean,
)
