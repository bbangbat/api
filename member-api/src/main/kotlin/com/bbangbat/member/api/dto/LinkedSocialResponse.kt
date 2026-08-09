package com.bbangbat.member.api.dto

import com.bbangbat.member.domain.SocialType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "연동된 소셜 계정")
data class LinkedSocialResponse(
    @field:Schema(description = "소셜 제공자") val provider: SocialType,
    @field:Schema(description = "현재 로그인에 사용한 제공자인지 여부") val current: Boolean,
) {
    companion object {
        fun from(
            provider: SocialType,
            currentProvider: String?,
        ): LinkedSocialResponse =
            LinkedSocialResponse(
                provider = provider,
                current = provider.name == currentProvider,
            )
    }
}
