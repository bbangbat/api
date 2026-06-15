package com.bbangbat.auth.token

import com.bbangbat.auth.voter.VoterResolver
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "인증", description = "JWT 토큰 관리 API")
@RestController
@RequestMapping("/auth")
class TokenController(
    private val tokenService: TokenService,
    private val refreshTokenCookieProvider: RefreshTokenCookieProvider,
    private val anonymousTokenProvider: AnonymousTokenProvider,
    private val anonymousTokenCookieProvider: AnonymousTokenCookieProvider,
) {
    @Operation(summary = "Access Token 갱신", description = "Refresh Token 쿠키로 새 Access Token을 발급합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token"),
    )
    @PostMapping("/token/refresh")
    @ResponseStatus(OK)
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
    @ResponseStatus(NO_CONTENT)
    fun logout(
        @AuthenticationPrincipal memberId: Long,
        response: HttpServletResponse,
    ) {
        tokenService.deleteRefreshToken(memberId)
        refreshTokenCookieProvider.clearCookie(response)
    }

    @Operation(
        summary = "익명 토큰 발급",
        description = "비회원 식별용 익명 토큰을 httpOnly 쿠키로 발급합니다. 이미 유효한 토큰이 있으면 유지합니다.",
    )
    @ApiResponse(responseCode = "204", description = "발급 완료")
    @PostMapping("/anonymous")
    @ResponseStatus(NO_CONTENT)
    fun issueAnonymousToken(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val existing =
            request.cookies
                ?.firstOrNull { it.name == VoterResolver.ANONYMOUS_COOKIE }
                ?.value
                ?.let { anonymousTokenProvider.getAnonymousId(it) }

        if (existing == null) {
            val anonymousToken = anonymousTokenProvider.createAnonymousToken(UUID.randomUUID().toString())

            anonymousTokenCookieProvider.addCookie(response, anonymousToken)
        }
    }
}
