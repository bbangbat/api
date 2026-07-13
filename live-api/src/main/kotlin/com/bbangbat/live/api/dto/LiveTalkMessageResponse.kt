package com.bbangbat.live.api.dto

import com.bbangbat.live.domain.LiveTalkMessage
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "실시간 톡 메시지 응답")
data class LiveTalkMessageResponse(
    @field:Schema(description = "메시지 ID", example = "1")
    val id: Long,
    @field:Schema(description = "작성자 닉네임", example = "빵순이")
    val authorNickname: String,
    @field:Schema(description = "메시지 내용", example = "지금 사람 많아요!")
    val content: String,
    @field:Schema(description = "작성 시각 (ISO-8601). 프론트에서 상대시간으로 변환해 표시", example = "2026-06-16T14:30:00")
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(message: LiveTalkMessage): LiveTalkMessageResponse =
            LiveTalkMessageResponse(
                id = message.id,
                authorNickname = message.authorNickname,
                content = message.content,
                createdAt = message.createdAt,
            )
    }
}
