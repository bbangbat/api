package com.bbangbat.auth.oauth2

import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

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
                response.addCookie(
                    Cookie(REDIRECT_URI_COOKIE, redirectUri).apply {
                        path = "/"
                        isHttpOnly = true
                        maxAge = COOKIE_MAX_AGE
                    },
                )
            }
        }

        filterChain.doFilter(request, response)
    }

    companion object {
        const val REDIRECT_URI_COOKIE = "oauth2_redirect_uri"
        private const val COOKIE_MAX_AGE = 180
    }
}
