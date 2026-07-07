package com.bbangbat.auth.jwt

import com.bbangbat.common.exception.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        // /api/ 경로의 인증 실패만 WARN으로 기록 (프론트 요청).
        // 봇/스캐너가 찌르는 그 외 경로(/, /.git/config, /robots.txt 등)는 DEBUG로 낮춰 로그 노이즈 억제
        if (request.requestURI.startsWith("/api/")) {
            log.warn("인증 실패: {} {} - {}", request.method, request.requestURI, authException.message)
        } else {
            log.debug("인증 실패(비 API 경로): {} {}", request.method, request.requestURI)
        }

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(
            """{"code":"${ErrorCode.UNAUTHORIZED.name}","message":"${ErrorCode.UNAUTHORIZED.message}"}""",
        )
    }
}
