package com.bbangbat.health.api

import com.bbangbat.health.api.dto.HealthResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "헬스체크", description = "애플리케이션 상태 확인 API")
@RestController
@RequestMapping("/api/health")
class HealthController {
    @Operation(summary = "애플리케이션 헬스체크")
    @GetMapping
    fun health(): HealthResponse = HealthResponse(status = "UP")
}
