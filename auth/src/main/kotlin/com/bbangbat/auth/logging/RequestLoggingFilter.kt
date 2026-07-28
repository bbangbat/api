package com.bbangbat.auth.logging

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * /api/ 요청의 요약(메서드·경로·상태·처리시간)을 한 줄로 남기고,
 * nginx가 전파한 X-Request-ID(없으면 생성)를 MDC에 넣어 요청 전체 로그를 하나의 ID로 추적한다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestLoggingFilter : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestId = request.getHeader(REQUEST_ID_HEADER)?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        MDC.put(REQUEST_ID_KEY, requestId)

        val start = System.currentTimeMillis()

        try {
            filterChain.doFilter(request, response)
        } finally {
            if (request.requestURI.startsWith("/api/")) {
                val duration = System.currentTimeMillis() - start
                val query = request.queryString?.let { "?$it" } ?: ""

                log.info("{} {}{} -> {} ({}ms)", request.method, request.requestURI, query, response.status, duration)
            }

            MDC.remove(REQUEST_ID_KEY)
        }
    }

    companion object {
        private const val REQUEST_ID_HEADER = "X-Request-ID"
        private const val REQUEST_ID_KEY = "requestId"
    }
}
