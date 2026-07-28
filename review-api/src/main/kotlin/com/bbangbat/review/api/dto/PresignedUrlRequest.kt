package com.bbangbat.review.api.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class PresignedUrlRequest(
    @field:NotEmpty(message = "파일 정보를 입력해주세요.")
    @field:Size(max = 5, message = "사진은 최대 5장까지 업로드할 수 있습니다.")
    val contentTypes: List<
        @Pattern(regexp = "image/(jpeg|png|webp|heic)", message = "지원하지 않는 이미지 형식입니다.")
        String,
    >,
)
