package com.bbangbat.auth.oauth2

import com.bbangbat.auth.jwt.JwtProvider
import com.bbangbat.auth.token.RefreshTokenCookieProvider
import com.bbangbat.auth.token.TokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtProvider: JwtProvider,
    private val tokenService: TokenService,
    private val refreshTokenCookieProvider: RefreshTokenCookieProvider,
    @Value("\${app.frontend-url}") private val frontendUrl: String,
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        // Authentication에서 memberId 추출
        val memberId = (authentication.principal as DefaultOAuth2User).getAttribute<Long>("memberId")!!

        // AT, RT 생성
        val accessToken = jwtProvider.createAccessToken(memberId)
        val refreshToken = jwtProvider.createRefreshToken(memberId)

        // Redis에 RT 저장
        tokenService.saveRefreshToken(memberId, refreshToken)

        // RT는 쿠키에 담아서 응답
        refreshTokenCookieProvider.addCookie(response, refreshToken)

        // 프론트엔드 url로 리다이렉트
        response.sendRedirect("$frontendUrl/oauth2/callback?access_token=$accessToken")
    }
}
