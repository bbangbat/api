package com.bbangbat.member.application

import com.bbangbat.member.repository.FavoritePersistenceAdapter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberStatsService(
    private val favoritePersistenceAdapter: FavoritePersistenceAdapter,
    private val memberActivityPort: MemberActivityPort,
) {
    @Transactional(readOnly = true)
    fun getStats(memberId: Long): MemberStats =
        MemberStats(
            reviewCount = memberActivityPort.countReviews(memberId),
            favoriteCount = favoritePersistenceAdapter.countByMemberId(memberId),
            talkCount = memberActivityPort.countTalks(memberId),
        )
}
