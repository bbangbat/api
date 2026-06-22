package com.bbangbat.live.presentation.dto

import com.bbangbat.live.domain.LiveTalkMessage
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(description = "실시간 톡 전송 요청")
data class LiveTalkMessageRequest(
    @field:Schema(description = "가게 ID", example = "1")
    @field:NotNull(message = "가게 ID는 필수입니다.")
    val storeId: Long?,
    @field:Schema(description = "메시지 내용", example = "지금 사람 많아요!")
    @field:NotBlank(message = "메시지는 필수입니다.")
    @field:Size(max = LiveTalkMessage.MAX_CONTENT_LENGTH, message = "메시지는 ${LiveTalkMessage.MAX_CONTENT_LENGTH}자를 초과할 수 없습니다.")
    val content: String?,
)
