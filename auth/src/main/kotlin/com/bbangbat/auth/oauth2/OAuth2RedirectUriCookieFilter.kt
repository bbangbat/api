package com.bbangbat.auth.oauth2

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration

/**
 * OAuth2 로그인 시작 요청(/oauth2/authorization/{provider})의 redirect_uri 파라미터를
 * 허용목록 검증 후 쿠키에 저장한다. 로그인 성공 핸들러가 이 쿠키로 최종 리다이렉트 대상을 정한다.
 * (세션 STATELESS라 세션 대신 쿠키 사용)
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
                // provider 콜백은 크로스사이트 top-level GET 네비게이션이라 SameSite=Lax로 전달된다.
                // (None은 CSRF 비활성 환경에서 노출면만 넓히므로 사용하지 않는다)
                response.addHeader(
                    HttpHeaders.SET_COOKIE,
                    ResponseCookie
                        .from(REDIRECT_URI_COOKIE, redirectUri)
                        .path("/")
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("Lax")
                        .maxAge(Duration.ofSeconds(COOKIE_MAX_AGE))
                        .build()
                        .toString(),
                )
            }
        }

        filterChain.doFilter(request, response)
    }

    companion object {
        const val REDIRECT_URI_COOKIE = "oauth2_redirect_uri"
        private const val COOKIE_MAX_AGE = 180L
    }
}
