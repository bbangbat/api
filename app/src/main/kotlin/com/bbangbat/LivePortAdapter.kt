package com.bbangbat

import com.bbangbat.live.repository.TalkPersistenceAdapter
import com.bbangbat.member.application.LivePort
import org.springframework.stereotype.Component

@Component
class LivePortAdapter(
    private val talkPersistenceAdapter: TalkPersistenceAdapter,
) : LivePort {
    override fun countByMemberId(memberId: Long): Long = talkPersistenceAdapter.countByAuthorId(memberId)
}
