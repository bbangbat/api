package com.bbangbat.member.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "로그인 상태에서 소셜 계정 연동 요청")
data class LinkSocialRequest(
    @field:NotBlank(message = "임시 토큰이 필요합니다.")
    @field:Schema(description = "purpose=link 로 소셜 인증 후 받은 임시 토큰")
    val tempToken: String,
)
