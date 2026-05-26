package com.bbangbat.member.domain

import java.time.LocalDateTime

data class Member(
    val id: Long = 0L,
    val email: String,
    val name: String,
    val nickname: String,
    val profileImageUrl: String? = null,
    val gender: Gender,
    val ageGroup: AgeGroup,
    val termsAgreed: Boolean,
    val privacyAgreed: Boolean,
    val lastLoginAt: LocalDateTime? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
) {
    init {
        require(email.isNotBlank()) { "이메일은 비어 있을 수 없습니다." }
        require(email.length <= 100) { "이메일은 100자를 초과할 수 없습니다." }
        require(name.isNotBlank()) { "이름은 비어 있을 수 없습니다." }
        require(name.length <= 30) { "이름은 30자를 초과할 수 없습니다." }
        require(nickname.isNotBlank()) { "닉네임은 비어 있을 수 없습니다." }
        require(nickname.length in 2..20) { "닉네임은 2자 이상 20자 이하여야 합니다." }
        profileImageUrl?.let {
            require(it.startsWith("https://")) { "프로필 이미지 URL은 https://로 시작해야 합니다." }
            require(it.length <= 500) { "프로필 이미지 URL은 500자를 초과할 수 없습니다." }
        }
        require(termsAgreed) { "서비스 이용약관에 동의해야 합니다." }
        require(privacyAgreed) { "개인정보처리방침에 동의해야 합니다." }
    }
}
