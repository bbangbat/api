package com.bbangbat.member.api

import com.bbangbat.auth.resolver.AuthMember
import com.bbangbat.member.api.dto.MemberStatsResponse
import com.bbangbat.member.application.MemberStatsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "회원", description = "회원 API")
@RestController
@RequestMapping("/api/members/me/stats")
class MemberStatsController(
    private val memberStatsService: MemberStatsService,
) {
    @Operation(summary = "내 활동 수 조회", description = "로그인한 회원의 리뷰, 즐겨찾기, 톡 작성 수를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getStats(
        @AuthMember memberId: Long,
    ): MemberStatsResponse = MemberStatsResponse.from(memberStatsService.getStats(memberId))
}
