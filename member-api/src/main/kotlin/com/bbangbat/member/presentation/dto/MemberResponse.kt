package com.bbangbat.member.presentation.dto

import com.bbangbat.member.domain.Member

data class MemberResponse(
    val id: Long,
    val email: String,
    val name: String,
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
