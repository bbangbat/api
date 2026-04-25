package com.bbangbat.member.domain

data class Member(
    val id: Long = 0L,
    val email: String,
    val name: String,
) {
    init {
        require(email.length <= 100) { "이메일은 100자를 초과할 수 없습니다." }
        require(name.length <= 30) { "이름은 30자를 초과할 수 없습니다." }
    }
}
