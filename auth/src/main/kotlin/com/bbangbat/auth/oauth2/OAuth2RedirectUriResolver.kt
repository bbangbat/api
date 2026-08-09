package com.bbangbat.auth.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component

/**
 * OAuth2 로그인 종료 시 리다이렉트할 프론트 origin을 결정한다.
 * 로그인 시작 때 저장된 redirect_uri 쿠키가 허용목록에 있으면 그 origin을, 아니면 기본 frontend-url을 사용한다.
 * 성공·실패 핸들러가 동일한 허용목록 검증을 공유한다.
 */
@Component
class OAuth2RedirectUriResolver(
    @param:Value("\${app.frontend-url}") private val frontendUrl: String,
    @param:Value("\${app.frontend-allowed-origins:}") allowedOrigins: String,
) {
    private val allowed =
        allowedOrigins
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

    /**
     * 리다이렉트 base origin을 반환하고, 사용한 redirect_uri 쿠키를 제거한다.
     */
    fun resolveAndClear(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): String {
        val cookie = request.cookies?.firstOrNull { it.name == OAuth2RedirectUriCookieFilter.REDIRECT_URI_COOKIE }

        if (cookie != null) {
            clearCookie(response)
        }

        val requested = cookie?.value

        return if (requested != null && requested in allowed) requested else frontendUrl
    }

    /**
     * 삭제 쿠키는 생성 시와 속성(Path/Secure/SameSite)이 같아야 브라우저가 같은 쿠키로 인식해 지운다.
     */
    private fun clearCookie(response: HttpServletResponse) {
        response.addHeader(
            HttpHeaders.SET_COOKIE,
            ResponseCookie
                .from(OAuth2RedirectUriCookieFilter.REDIRECT_URI_COOKIE, "")
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
