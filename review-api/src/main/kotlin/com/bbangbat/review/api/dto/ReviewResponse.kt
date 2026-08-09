package com.bbangbat.review.api.dto

import com.bbangbat.review.application.AuthoredReview
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "빵명록 응답")
data class ReviewResponse(
    val id: Long,
    val memberId: Long,
    @field:Schema(description = "작성자 닉네임 (탈퇴한 회원은 '탈퇴한 회원')") val authorNickname: String,
    @field:Schema(description = "작성자 프로필 이미지 URL (없으면 null)") val authorProfileImageUrl: String?,
    val rating: Int,
    val menus: List<String>,
    val content: String,
    val imageUrls: List<String>,
    val createdAt: LocalDateTime?,
) {
    companion object {
        fun from(authored: AuthoredReview) =
            ReviewResponse(
                id = authored.review.id,
                memberId = authored.review.memberId,
                authorNickname = authored.authorNickname,
                authorProfileImageUrl = authored.authorProfileImageUrl,
                rating = authored.review.rating,
                menus = authored.review.menus,
                content = authored.review.content,
                imageUrls = authored.review.imageUrls,
                createdAt = authored.review.createdAt,
            )
    }
}
