package com.bbangbat.member.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

@Schema(description = "프로필 이미지 업로드 URL 발급 요청")
data class ProfileImageUploadRequest(
    @field:NotBlank(message = "contentType은 필수입니다.")
    @field:Pattern(regexp = "image/(jpeg|png|webp)", message = "jpeg, png, webp 이미지만 업로드할 수 있습니다.")
    @field:Schema(description = "업로드할 이미지의 Content-Type", example = "image/jpeg")
    val contentType: String? = null,
)
