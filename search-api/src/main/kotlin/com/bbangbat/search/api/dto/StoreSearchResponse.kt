package com.bbangbat.search.api.dto

import com.bbangbat.store.domain.Store
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "가게 검색 응답")
data class StoreSearchResponse(
    @field:Schema(description = "가게 ID", example = "1") val id: Long,
    @field:Schema(description = "가게명", example = "홍길동 베이커리") val name: String,
    @field:Schema(description = "주소", example = "서울시 강남구 테헤란로 1") val address: String,
) {
    companion object {
        fun from(store: Store): StoreSearchResponse =
            StoreSearchResponse(
                id = store.id,
                name = store.name,
                address = store.address,
            )
    }
}
