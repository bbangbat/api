package com.bbangbat.auth.token

import com.bbangbat.auth.voter.VoterResolver
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class AnonymousTokenCookieProvider {
    fun addCookie(
        response: HttpServletResponse,
        anonymousToken: String,
    ) {
        val cookie = Cookie(VoterResolver.ANONYMOUS_COOKIE, anonymousToken)

        cookie.isHttpOnly = true
        cookie.secure = true
        cookie.path = "/"
        cookie.maxAge = (AnonymousTokenProvider.EXPIRY / 1000).toInt()

        response.addCookie(cookie)
    }
}
