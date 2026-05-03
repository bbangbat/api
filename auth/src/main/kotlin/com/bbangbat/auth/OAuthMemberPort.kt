package com.bbangbat.auth

interface OAuthMemberPort {
    fun find(
        email: String,
        name: String,
    ): Long
}
