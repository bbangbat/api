package com.bbangbat.member.application

interface ReviewPort {
    fun countByMemberId(memberId: Long): Long
}
