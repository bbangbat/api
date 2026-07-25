package com.bbangbat.member.application

interface MemberActivityPort {
    fun countReviews(memberId: Long): Long

    fun countTalks(memberId: Long): Long
}
