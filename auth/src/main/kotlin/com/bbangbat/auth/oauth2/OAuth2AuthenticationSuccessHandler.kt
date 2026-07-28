package com.bbangbat.auth.oauth2

import com.bbangbat.auth.jwt.JwtProvider
import com.bbangbat.auth.token.RefreshTokenCookieProvider
import com.bbangbat.auth.token.TempTokenProvider
import com.bbangbat.auth.token.TokenService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2AuthenticationSuccessHandler(
    private val memberPort: MemberPort,
    private val jwtProvider: JwtProvider,
    private val tokenService: TokenService,
    private val refreshTokenCookieProvider: RefreshTokenCookieProvider,
    private val tempTokenProvider: TempTokenProvider,
    @param:Value("\${app.frontend-url}") private val frontendUrl: String,
    @param:Value("\${app.frontend-allowed-origins:}") allowedOrigins: String,
) : AuthenticationSuccessHandler {
    private val allowed =
        allowedOrigins
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val oAuth2User = authentication.principal as DefaultOAuth2User
        val memberId = oAuth2User.getAttribute<Long?>("memberId")
        val base = resolveFrontendBase(request, response)

        if (memberId != null) {
            val accessToken = jwtProvider.createAccessToken(memberId)
            val refreshToken = jwtProvider.createRefreshToken(memberId)

            tokenService.saveRefreshToken(memberId, refreshToken)
            refreshTokenCookieProvider.addCookie(response, refreshToken)
            memberPort.updateLastLoginAt(memberId)

            response.sendRedirect("$base/oauth2/callback?access_token=$accessToken")
        } else {
            val email = oAuth2User.getAttribute<String>("email")!!
            val name = oAuth2User.getAttribute<String>("name")!!
            val provider = oAuth2User.getAttribute<SocialProvider>("provider")!!
            val providerId = oAuth2User.getAttribute<String>("providerId")!!
            val ageGroup = oAuth2User.getAttribute<String?>("ageGroup")
            val existingAccount = oAuth2User.getAttribute<Boolean>("existingAccount") ?: false
            val tempToken = tempTokenProvider.createTempToken(email, name, provider, providerId, ageGroup)

            response.sendRedirect(
                "$base/signup?temp_token=$tempToken&existing_account=$existingAccount",
            )
        }
    }

    /**
     * OAuth2 시작 시 저장된 redirect_uri 쿠키가 허용목록에 있으면 그 origin으로, 아니면 기본 frontend-url로 리다이렉트한다.
     * 사용 후 쿠키는 제거한다.
     */
    private fun resolveFrontendBase(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): String {
        val cookie = request.cookies?.firstOrNull { it.name == OAuth2RedirectUriCookieFilter.REDIRECT_URI_COOKIE }

        if (cookie != null) {
            response.addCookie(
                Cookie(OAuth2RedirectUriCookieFilter.REDIRECT_URI_COOKIE, "").apply {
                    path = "/"
                    maxAge = 0
                },
            )
        }

        val requested = cookie?.value

        return if (requested != null && requested in allowed) requested else frontendUrl
    }
}
