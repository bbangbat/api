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

    private fun createCookie(
        value: String,
        maxAge: Int,
    ): Cookie {
        val cookie = Cookie("refresh_token", value)

        cookie.isHttpOnly = true
        cookie.secure = true
        cookie.path = "/auth/token"
        cookie.maxAge = maxAge

        return cookie
    }
}
