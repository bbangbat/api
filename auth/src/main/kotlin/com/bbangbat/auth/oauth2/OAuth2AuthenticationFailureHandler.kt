package com.bbangbat.auth.oauth2

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component

/**
 * 소셜 로그인 실패 시 Whitelabel 대신 허용된 프론트 origin으로 리다이렉트한다.
 * 원인 파악을 위해 errorCode와 stack trace만 남기고,
 * authorization code·토큰·state·사용자 개인정보는 로그에 남기지 않는다.
 */
@Component
class OAuth2AuthenticationFailureHandler(
    private val redirectUriResolver: OAuth2RedirectUriResolver,
) : AuthenticationFailureHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        val errorCode = (exception as? OAuth2AuthenticationException)?.error?.errorCode ?: UNKNOWN_ERROR_CODE
        val base = redirectUriResolver.resolveAndClear(request, response)

        log.warn("소셜 로그인 실패: errorCode={}", errorCode, exception)

        response.sendRedirect("$base/oauth2/callback?error=oauth_failed")
    }

    companion object {
        private const val UNKNOWN_ERROR_CODE = "unknown"
    }
}
