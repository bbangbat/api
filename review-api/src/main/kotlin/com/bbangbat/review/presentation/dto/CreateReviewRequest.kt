package com.bbangbat.review.presentation.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class CreateReviewRequest(
    val storeId: Long,
    @field:Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
    @field:Max(value = 5, message = "별점은 5점 이하여야 합니다.")
    val rating: Int,
    @field:NotEmpty(message = "구매한 메뉴를 입력해주세요.")
    val menus: List<
        @NotBlank(message = "메뉴명은 빈 값일 수 없습니다.")
        String,
    >,
    @field:Size(min = 10, max = 500, message = "후기는 10자 이상 500자 이하여야 합니다.")
    val content: String,
    @field:Size(max = 5, message = "사진은 최대 5장까지 업로드할 수 있습니다.")
    val imageKeys: List<String> = emptyList(),
)
