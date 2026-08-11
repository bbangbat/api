package com.bbangbat.live.application

import com.bbangbat.live.domain.LiveTalkMessage
import java.time.LocalDateTime

/** 마이페이지의 "내가 쓴 톡" 목록 한 건. 톡 자체에는 없는 가게명을 붙여서 내려준다. */
data class MyTalkMessage(
    val id: Long,
    val storeId: Long,
    val storeName: String,
    val content: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        /** 가게가 사라졌더라도 내가 쓴 톡은 목록에서 빠지지 않아야 한다. */
        const val UNKNOWN_STORE_NAME = "알 수 없는 가게"

        fun of(
            message: LiveTalkMessage,
            storeName: String?,
        ): MyTalkMessage =
            MyTalkMessage(
                id = message.id,
                storeId = message.storeId,
                storeName = storeName ?: UNKNOWN_STORE_NAME,
                content = message.content,
                createdAt = message.createdAt,
            )
    }
}
