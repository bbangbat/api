package com.bbangbat.member.application

interface LivePort {
    fun countByMemberId(memberId: Long): Long

    /** 회원 탈퇴 시 해당 회원이 남긴 혼잡도 투표를 제거한다. (톡 메시지는 닉네임 스냅샷으로 보존) */
    fun deleteCongestionVotesByMemberId(memberId: Long)
}
