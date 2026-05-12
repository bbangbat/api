package com.bbangbat.member.domain

data class Social(
    val id: Long = 0L,
    val member: Member,
    val provider: SocialType,
    val providerId: String,
) {
    init {
        require(providerId.isNotBlank()) { "소셜 제공자 ID는 비어 있을 수 없습니다." }
    }
}
