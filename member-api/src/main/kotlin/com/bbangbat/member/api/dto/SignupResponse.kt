package com.bbangbat.member.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원가입 응답")
data class SignupResponse(
    @field:Schema(description = "액세스 토큰") val accessToken: String,
)
