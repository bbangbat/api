package com.bbangbat.auth.token

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "인증", description = "JWT 토큰 관리 API")
@RestController
@RequestMapping("/auth")
class TokenController(
    private val tokenService: TokenService,
    private val refreshTokenCookieProvider: RefreshTokenCookieProvider,
) {
    @Operation(summary = "Access Token 갱신", description = "Refresh Token 쿠키로 새 Access Token을 발급합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token"),
    )
    @PostMapping("/token/refresh")
    fun refresh(
        @CookieValue("refresh_token") refreshToken: String,
        response: HttpServletResponse,
    ): TokenResponse {
        val (newAt, newRt) = tokenService.rotateToken(refreshToken)

        refreshTokenCookieProvider.addCookie(response, newRt)

        return TokenResponse(accessToken = newAt)
    }

    @Operation(summary = "로그아웃", description = "Refresh Token을 무효화하고 쿠키를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(
        @AuthenticationPrincipal memberId: Long,
        response: HttpServletResponse,
    ) {
        tokenService.deleteRefreshToken(memberId)

        refreshTokenCookieProvider.clearCookie(response)
    }
}
