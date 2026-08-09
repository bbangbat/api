package com.bbangbat.auth.token

import com.bbangbat.auth.jwt.JwtProperties
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class RefreshTokenCookieProvider(
    private val jwtProperties: JwtProperties,
) {
    fun addCookie(
        response: HttpServletResponse,
        refreshToken: String,
    ) {
        response.addCookie(createCookie(refreshToken, (jwtProperties.refreshTokenExpiry / 1000).toInt()))
    }

    fun clearCookie(response: HttpServletResponse) {
        response.addCookie(createCookie("", 0))
    }

    /**
     * 로컬 프론트(localhost)에서 dev 서버로 토큰을 재발급받는 요청은 크로스사이트 XHR이라
     * SameSite 기본값(Lax)으로는 쿠키가 실리지 않는다. Domain은 지정하지 않아 host-only로 둔다.
     */
    private fun createCookie(
        value: String,
        maxAge: Int,
    ): Cookie =
        Cookie("refresh_token", value).apply {
            isHttpOnly = true
            secure = true
            path = "/auth/token"
            this.maxAge = maxAge
            setAttribute("SameSite", "None")
        }
}
