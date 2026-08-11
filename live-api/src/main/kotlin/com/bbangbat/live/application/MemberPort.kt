package com.bbangbat.live.application

interface MemberPort {
    fun getNickname(memberId: Long): String

    /** 신고 대응 등으로 타인의 톡을 삭제할 수 있는 운영자인지 */
    fun isAdmin(memberId: Long): Boolean
}
