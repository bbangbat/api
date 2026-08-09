package com.bbangbat.auth.token

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "OAuth 결과 교환 요청")
data class OAuthExchangeRequest(
    @field:NotBlank(message = "code가 필요합니다.")
    @field:Schema(description = "리다이렉트로 받은 1회용 code")
    val code: String,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "OAuth 결과 교환 응답")
data class OAuthExchangeResponse(
    @field:Schema(description = "LOGIN(기존 회원) / SIGNUP(신규 가입) / LINK(소셜 연동) / UNLINK(연동 해제 재인증)") val type: AuthCodeType,
    @field:Schema(description = "type이 LOGIN일 때 반환") val accessToken: String? = null,
    @field:Schema(description = "type이 SIGNUP 또는 LINK일 때 반환") val tempToken: String? = null,
    @field:Schema(description = "type이 SIGNUP일 때, 이미 가입된 이메일인지 여부") val existingAccount: Boolean? = null,
    @field:Schema(description = "type이 SIGNUP일 때, 소셜에서 받은 성별. 미제공이면 null", example = "FEMALE")
    val gender: String? = null,
    @field:Schema(description = "type이 SIGNUP일 때, 소셜에서 받은 연령대. 미제공이면 null", example = "TWENTIES")
    val ageGroup: String? = null,
    @field:Schema(description = "type이 UNLINK일 때, 재인증한 소셜 제공자") val provider: String? = null,
)
