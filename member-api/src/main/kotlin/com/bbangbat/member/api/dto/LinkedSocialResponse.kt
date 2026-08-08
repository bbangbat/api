package com.bbangbat.member.api.dto

import com.bbangbat.member.domain.SocialType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "연동된 소셜 계정")
data class LinkedSocialResponse(
    @field:Schema(description = "소셜 제공자") val provider: SocialType,
) {
    companion object {
        fun from(provider: SocialType): LinkedSocialResponse = LinkedSocialResponse(provider = provider)
    }
}
