package com.bbangbat.member.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "소셜 계정 연동 요청")
data class LinkRequest(
    @field:NotBlank(message = "임시 토큰이 필요합니다.")
    @field:Schema(description = "임시 토큰 (연동할 소셜 로그인 후 발급)")
    val tempToken: String,
)
