package com.bbangbat.auth.oauth2

import com.bbangbat.auth.token.AuthCodePayload
import com.bbangbat.auth.token.AuthCodeService
import com.bbangbat.auth.token.AuthCodeType
import com.bbangbat.auth.token.SocialTokenService
import com.bbangbat.auth.token.TempTokenProvider
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
    private val tempTokenProvider: TempTokenProvider,
    private val authCodeService: AuthCodeService,
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
        val purpose = readAndClearPurpose(request, response)

        // 연동 해제를 위한 재인증: 소셜 토큰만 확보하고 로그인 처리는 하지 않는다.
        // (여기서 로그인시키면 기존 세션의 액세스/리프레시 토큰과 provider claim이 갈아엎어진다)
        if (purpose == OAuth2RedirectUriCookieFilter.PURPOSE_UNLINK) {
            keepSocialToken(oAuth2User)
            redirectForUnlink(response, base, oAuth2User)

            return
        }

        // 마이페이지 소셜 연동: 로그인 처리 대신 임시 토큰만 발급한다.
        if (purpose == OAuth2RedirectUriCookieFilter.PURPOSE_LINK) {
            redirectForLink(response, base, memberId, oAuth2User)

            return
        }

        if (memberId != null) {
            val loginProvider = oAuth2User.getAttribute<SocialProvider>("provider")?.name
            val code =
                authCodeService.issue(
                    AuthCodePayload(type = AuthCodeType.LOGIN, memberId = memberId, provider = loginProvider),
                )

            response.sendRedirect("$base/oauth2/callback?code=$code")
        } else {
            val email = oAuth2User.getAttribute<String>("email")!!
            val name = oAuth2User.getAttribute<String>("name")!!
            val provider = oAuth2User.getAttribute<SocialProvider>("provider")!!
            val providerId = oAuth2User.getAttribute<String>("providerId")!!
            val ageGroup = oAuth2User.getAttribute<String?>("ageGroup")
            val gender = oAuth2User.getAttribute<String?>("gender")
            val existingAccount = oAuth2User.getAttribute<Boolean>("existingAccount") ?: false
            val tempToken = tempTokenProvider.createTempToken(email, name, provider, providerId, ageGroup, gender)
            val code =
                authCodeService.issue(
                    AuthCodePayload(
                        type = AuthCodeType.SIGNUP,
                        tempToken = tempToken,
                        existingAccount = existingAccount,
                    ),
                )

            response.sendRedirect("$base/signup?code=$code")
        }
    }

    /** 로그인 시작 시 지정된 purpose를 읽고 쿠키를 즉시 제거한다. */
    private fun readAndClearPurpose(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): String? {
        val cookie = request.cookies?.firstOrNull { it.name == OAuth2RedirectUriCookieFilter.PURPOSE_COOKIE }

        if (cookie != null) {
            clearPurposeCookie(response)
        }

        return cookie?.value
    }

    /**
     * 연동 해제/탈퇴를 위한 재인증일 때만 소셜 access token을 잠시 보관한다.
     * 평범한 로그인에서는 저장하지 않는다.
     */
    private fun keepSocialToken(oAuth2User: DefaultOAuth2User) {
        val provider = oAuth2User.getAttribute<SocialProvider>("provider") ?: return
        val providerId = oAuth2User.getAttribute<String>("providerId") ?: return
        val accessToken = oAuth2User.getAttribute<String>(CustomOAuth2UserService.SOCIAL_ACCESS_TOKEN_ATTRIBUTE) ?: return

        socialTokenService.save(provider, providerId, accessToken)
    }

    /**
     * 연동 해제 재인증 흐름의 리다이렉트.
     * 빵밭 토큰은 건드리지 않고, 어느 제공자로 재인증했는지만 알려준다.
     */
    private fun redirectForUnlink(
        response: HttpServletResponse,
        base: String,
        oAuth2User: DefaultOAuth2User,
    ) {
        val provider = oAuth2User.getAttribute<SocialProvider>("provider")?.name
        val code = authCodeService.issue(AuthCodePayload(type = AuthCodeType.UNLINK, provider = provider))

        response.sendRedirect("$base/oauth2/unlink/callback?code=$code")
    }

    /**
     * 소셜 연동 흐름의 리다이렉트.
     * 이미 다른 계정에 연동된 소셜이면 그 계정으로 로그인시키지 않고 오류로 돌려보낸다.
     */
    private fun redirectForLink(
        response: HttpServletResponse,
        base: String,
        memberId: Long?,
        oAuth2User: DefaultOAuth2User,
    ) {
        if (memberId != null) {
            response.sendRedirect("$base/oauth2/link/callback?error=already_linked")

            return
        }

        val email = oAuth2User.getAttribute<String>("email")!!
        val name = oAuth2User.getAttribute<String>("name")!!
        val provider = oAuth2User.getAttribute<SocialProvider>("provider")!!
        val providerId = oAuth2User.getAttribute<String>("providerId")!!
        val ageGroup = oAuth2User.getAttribute<String?>("ageGroup")
        val gender = oAuth2User.getAttribute<String?>("gender")
        val tempToken = tempTokenProvider.createTempToken(email, name, provider, providerId, ageGroup, gender)
        val code = authCodeService.issue(AuthCodePayload(type = AuthCodeType.LINK, tempToken = tempToken))

        response.sendRedirect("$base/oauth2/link/callback?code=$code")
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
