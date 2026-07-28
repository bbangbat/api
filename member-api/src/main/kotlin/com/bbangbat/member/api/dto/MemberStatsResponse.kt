package com.bbangbat.member.api.dto

import com.bbangbat.member.application.MemberStats

data class MemberStatsResponse(
    val reviewCount: Long,
    val favoriteCount: Long,
    val talkCount: Long,
) {
    companion object {
        fun from(stats: MemberStats): MemberStatsResponse =
            MemberStatsResponse(
                reviewCount = stats.reviewCount,
                favoriteCount = stats.favoriteCount,
                talkCount = stats.talkCount,
            )
    }
}
