package com.bbangbat.member.presentation

import com.bbangbat.auth.resolver.AuthMember
import com.bbangbat.member.application.FavoriteService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "즐겨찾기", description = "나만의 빵지도 즐겨찾기 API (회원 전용)")
@RestController
@RequestMapping("/api/members/favorites")
class FavoriteController(
    private val favoriteService: FavoriteService,
) {
    @Operation(summary = "즐겨찾기 추가")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "추가 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "409", description = "이미 즐겨찾기에 추가된 가게"),
    )
    @PostMapping("/{storeId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(
        @PathVariable storeId: Long,
        @AuthMember memberId: Long,
    ) {
        favoriteService.add(memberId, storeId)
    }

    @Operation(summary = "즐겨찾기 삭제")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "404", description = "즐겨찾기에 없는 가게"),
    )
    @DeleteMapping("/{storeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(
        @PathVariable storeId: Long,
        @AuthMember memberId: Long,
    ) {
        favoriteService.remove(memberId, storeId)
    }

    @Operation(summary = "즐겨찾기 목록 조회", description = "즐겨찾기한 가게의 ID 목록을 반환합니다. 가게 상세는 store-api와 클라이언트에서 합칩니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getFavorites(
        @AuthMember memberId: Long,
    ): List<Long> = favoriteService.findStoreIds(memberId)
}
