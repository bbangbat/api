package com.bbangbat.store.api

import com.bbangbat.store.api.dto.StoreResponse
import com.bbangbat.store.application.StoreService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "가게", description = "가게 API")
@RestController
@RequestMapping("/api/stores")
class StoreController(
    private val storeService: StoreService,
) {
    @Operation(summary = "반경 내 가게 리스트 조회", description = "지도 중심 좌표 기준 반경 3km 내 가게를 거리순으로 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 좌표 요청"),
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getStores(
        @RequestParam lat: Double,
        @RequestParam lng: Double,
    ): List<StoreResponse> = storeService.findStores(lat, lng).map { StoreResponse.from(it) }
}
