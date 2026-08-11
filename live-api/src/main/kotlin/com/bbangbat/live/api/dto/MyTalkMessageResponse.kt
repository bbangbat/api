package com.bbangbat.live.api.dto

import com.bbangbat.live.application.MyTalkMessage
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "내가 쓴 실시간 톡")
data class MyTalkMessageResponse(
    @field:Schema(description = "톡 ID") val id: Long,
    @field:Schema(description = "작성한 가게 ID") val storeId: Long,
    @field:Schema(description = "작성한 가게명") val storeName: String,
    @field:Schema(description = "내용") val content: String,
    @field:Schema(description = "작성 시각 (ISO-8601)") val createdAt: LocalDateTime,
) {
    companion object {
        fun from(message: MyTalkMessage): MyTalkMessageResponse =
            MyTalkMessageResponse(
                id = message.id,
                storeId = message.storeId,
                storeName = message.storeName,
                content = message.content,
                createdAt = message.createdAt,
            )
    }
}
