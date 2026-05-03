package com.bbangbat.auth.token

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "토큰 응답")
data class TokenResponse(
    @field:Schema(description = "Access Token (JWT)")
    val accessToken: String,
)
