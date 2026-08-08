package com.bbangbat.member.api.dto

import com.bbangbat.member.domain.NicknamePolicy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "프로필 수정 요청 (변경할 필드만 전달)")
data class UpdateProfileRequest(
    @field:Size(
        min = NicknamePolicy.MIN_LENGTH,
        max = NicknamePolicy.MAX_LENGTH,
        message = NicknamePolicy.LENGTH_MESSAGE,
    )
    @field:Pattern(regexp = NicknamePolicy.REGEX, message = NicknamePolicy.FORMAT_MESSAGE)
    @field:Schema(description = "닉네임 (한글/영문/숫자 2~10자)", example = "빵괴물")
    val nickname: String? = null,
    @field:Size(max = 500, message = "프로필 이미지 키는 500자를 초과할 수 없습니다.")
    @field:Schema(description = "presigned URL 발급 시 받은 오브젝트 key", example = "members/3f2a...")
    val profileImageKey: String? = null,
)
