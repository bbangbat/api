package com.bbangbat.auth.oauth2

import com.bbangbat.auth.jwt.JwtProvider
import com.bbangbat.auth.token.RefreshTokenCookieProvider
import com.bbangbat.auth.token.TempTokenProvider
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
    private val memberPort: MemberPort,
    private val jwtProvider: JwtProvider,
    private val tokenService: TokenService,
    private val refreshTokenCookieProvider: RefreshTokenCookieProvider,
    private val tempTokenProvider: TempTokenProvider,
    @param:Value("\${app.frontend-url}") private val frontendUrl: String,
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val oAuth2User = authentication.principal as DefaultOAuth2User
        val memberId = oAuth2User.getAttribute<Long?>("memberId")

        if (memberId != null) {
            val accessToken = jwtProvider.createAccessToken(memberId)
            val refreshToken = jwtProvider.createRefreshToken(memberId)

            tokenService.saveRefreshToken(memberId, refreshToken)
            refreshTokenCookieProvider.addCookie(response, refreshToken)
            memberPort.updateLastLoginAt(memberId)

            response.sendRedirect("$frontendUrl/oauth2/callback?access_token=$accessToken")
        } else {
            val email = oAuth2User.getAttribute<String>("email")!!
            val name = oAuth2User.getAttribute<String>("name")!!
            val provider = oAuth2User.getAttribute<SocialProvider>("provider")!!
            val providerId = oAuth2User.getAttribute<String>("providerId")!!
            val ageGroup = oAuth2User.getAttribute<String?>("ageGroup")
            val existingAccount = oAuth2User.getAttribute<Boolean>("existingAccount") ?: false
            val tempToken = tempTokenProvider.createTempToken(email, name, provider, providerId, ageGroup)

            response.sendRedirect(
                "$frontendUrl/signup?temp_token=$tempToken&existing_account=$existingAccount",
            )
        }
    }
}
