package com.bbangbat.search.presentation

import com.bbangbat.search.application.SearchService
import com.bbangbat.search.presentation.dto.StoreSearchResponse
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

@Tag(name = "검색", description = "가게 검색 API")
@RestController
@RequestMapping("/api/stores/search")
class SearchController(
    private val searchService: SearchService,
) {
    @Operation(summary = "가게명 검색", description = "가게명에 검색어가 포함된 가게를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun searchStores(
        @RequestParam keyword: String,
    ): List<StoreSearchResponse> = searchService.searchStores(keyword).map { StoreSearchResponse.from(it) }
}
