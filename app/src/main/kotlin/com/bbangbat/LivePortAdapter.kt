package com.bbangbat

import com.bbangbat.live.application.CongestionService
import com.bbangbat.live.application.TalkStatsService
import com.bbangbat.member.application.LivePort
import org.springframework.stereotype.Component

/** 회원 모듈이 실시간(live) 데이터를 필요로 할 때 쓰는 포트의 구현 */
@Component
class LivePortAdapter(
    private val talkStatsService: TalkStatsService,
    private val congestionService: CongestionService,
) : LivePort {
    override fun countByMemberId(memberId: Long): Long = talkStatsService.countByAuthorId(memberId)

    override fun deleteCongestionVotesByMemberId(memberId: Long) = congestionService.deleteVotesByMember(memberId)
}
