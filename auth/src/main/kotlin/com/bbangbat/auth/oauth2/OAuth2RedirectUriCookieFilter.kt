package com.bbangbat.auth.oauth2

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration

/**
 * OAuth2 로그인 시작 요청(/oauth2/authorization/{provider})의 파라미터를 쿠키로 옮겨
 * provider 콜백 이후의 성공 핸들러까지 전달한다. (세션 STATELESS라 세션 대신 쿠키 사용)
 *
 * - redirect_uri: 허용목록 검증 후 최종 리다이렉트 대상 결정에 사용
 * - purpose=unlink: 연동 해제/탈퇴를 위한 재인증임을 표시. 이때만 소셜 토큰을 잠시 보관한다.
 */
class OAuth2RedirectUriCookieFilter(
    allowedOrigins: String,
) : OncePerRequestFilter() {
    private val allowed =
        allowedOrigins
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (request.requestURI.startsWith("/oauth2/authorization/")) {
            val redirectUri = request.getParameter("redirect_uri")

            if (redirectUri != null && redirectUri in allowed) {
                response.addHeader(HttpHeaders.SET_COOKIE, shortLivedCookie(REDIRECT_URI_COOKIE, redirectUri))
            }

            if (request.getParameter("purpose") == PURPOSE_UNLINK) {
                response.addHeader(HttpHeaders.SET_COOKIE, shortLivedCookie(PURPOSE_COOKIE, PURPOSE_UNLINK))
            }
        }

        filterChain.doFilter(request, response)
    }

    /**
     * provider 콜백은 크로스사이트 top-level GET 네비게이션이라 SameSite=Lax로 전달된다.
     * (None은 CSRF 비활성 환경에서 노출면만 넓히므로 사용하지 않는다)
     */
    private fun shortLivedCookie(
        name: String,
        value: String,
    ): String =
        ResponseCookie
            .from(name, value)
            .path("/")
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .maxAge(Duration.ofSeconds(COOKIE_MAX_AGE))
            .build()
            .toString()

    companion object {
        const val REDIRECT_URI_COOKIE = "oauth2_redirect_uri"
        const val PURPOSE_COOKIE = "oauth2_purpose"
        const val PURPOSE_UNLINK = "unlink"
        private const val COOKIE_MAX_AGE = 180L
    }
}
