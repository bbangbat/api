package com.bbangbat

import com.bbangbat.auth.voter.VoterType
import com.bbangbat.live.repository.CongestionVotePersistenceAdapter
import com.bbangbat.live.repository.TalkPersistenceAdapter
import com.bbangbat.member.application.LivePort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LivePortAdapter(
    private val talkPersistenceAdapter: TalkPersistenceAdapter,
    private val congestionVotePersistenceAdapter: CongestionVotePersistenceAdapter,
) : LivePort {
    override fun countByMemberId(memberId: Long): Long = talkPersistenceAdapter.countByAuthorId(memberId)

    @Transactional
    override fun deleteCongestionVotesByMemberId(memberId: Long) {
        congestionVotePersistenceAdapter.deleteAllByVoter(VoterType.MEMBER, memberId.toString())
    }
}
