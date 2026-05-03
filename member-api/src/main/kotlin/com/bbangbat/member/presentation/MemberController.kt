package com.bbangbat.member.presentation

import com.bbangbat.member.application.MemberService
import com.bbangbat.member.presentation.dto.MemberResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "회원", description = "회원 API")
@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService,
) {
    @Operation(summary = "내 정보 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "회원 없음"),
    )
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    fun getMe(
        @AuthenticationPrincipal memberId: Long,
    ): MemberResponse = MemberResponse.from(memberService.findById(memberId))
}
