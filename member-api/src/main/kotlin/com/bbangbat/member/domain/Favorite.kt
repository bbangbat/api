package com.bbangbat.member.domain

data class Favorite(
    val id: Long = 0L,
    val memberId: Long,
    val storeId: Long,
)
