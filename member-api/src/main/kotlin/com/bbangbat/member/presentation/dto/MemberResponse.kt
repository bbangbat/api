package com.bbangbat.member.presentation.dto

import com.bbangbat.member.domain.Member
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원 응답")
data class MemberResponse(
    @field:Schema(description = "회원 ID", example = "1234567890") val id: Long,
    @field:Schema(description = "이메일", example = "user@example.com") val email: String,
    @field:Schema(description = "이름", example = "홍길동") val name: String,
) {
    companion object {
        fun from(member: Member): MemberResponse =
            MemberResponse(
                id = member.id,
                email = member.email,
                name = member.name,
            )
    }
}
