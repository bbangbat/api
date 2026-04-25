package com.bbangbat.member.presentation

import com.bbangbat.member.application.MemberService
import com.bbangbat.member.presentation.dto.MemberResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService,
) {
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getMember(
        @PathVariable id: Long,
    ): MemberResponse = MemberResponse.from(memberService.findById(id))
}
