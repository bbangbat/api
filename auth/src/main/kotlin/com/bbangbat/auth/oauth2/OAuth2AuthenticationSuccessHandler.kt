package com.bbangbat.auth.oauth2

import com.bbangbat.auth.jwt.JwtProvider
import com.bbangbat.auth.token.RefreshTokenCookieProvider
import com.bbangbat.auth.token.SocialTokenService
import com.bbangbat.auth.token.TempTokenProvider
import com.bbangbat.auth.token.TokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
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
    private val redirectUriResolver: OAuth2RedirectUriResolver,
    private val socialTokenService: SocialTokenService,
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val oAuth2User = authentication.principal as DefaultOAuth2User
        val memberId = oAuth2User.getAttribute<Long?>("memberId")
        val base = redirectUriResolver.resolveAndClear(request, response)

        keepSocialTokenIfUnlinkPurpose(request, response, oAuth2User)

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
     * 연동 해제/탈퇴를 위한 재인증(purpose=unlink)일 때만 소셜 access token을 잠시 보관한다.
     * 평범한 로그인에서는 저장하지 않는다.
     */
    private fun keepSocialTokenIfUnlinkPurpose(
        request: HttpServletRequest,
        response: HttpServletResponse,
        oAuth2User: DefaultOAuth2User,
    ) {
        val purpose = request.cookies?.firstOrNull { it.name == OAuth2RedirectUriCookieFilter.PURPOSE_COOKIE }

        if (purpose != null) {
            clearPurposeCookie(response)
        }

        if (purpose?.value != OAuth2RedirectUriCookieFilter.PURPOSE_UNLINK) {
            return
        }

        val provider = oAuth2User.getAttribute<SocialProvider>("provider") ?: return
        val providerId = oAuth2User.getAttribute<String>("providerId") ?: return
        val accessToken = oAuth2User.getAttribute<String>(CustomOAuth2UserService.SOCIAL_ACCESS_TOKEN_ATTRIBUTE) ?: return

        socialTokenService.save(provider, providerId, accessToken)
    }

    private fun clearPurposeCookie(response: HttpServletResponse) {
        response.addHeader(
            HttpHeaders.SET_COOKIE,
            ResponseCookie
                .from(OAuth2RedirectUriCookieFilter.PURPOSE_COOKIE, "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(0)
                .build()
                .toString(),
        )
    }
}
