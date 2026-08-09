package com.bbangbat.member.application

import com.bbangbat.member.repository.FavoritePersistenceAdapter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 회원의 활동 수 집계.
 *
 * 여러 모듈의 데이터를 모으는 책임이라 MemberService와 분리한다.
 * (MemberService가 ReviewPort를 의존하면 ReviewService -> MemberPort -> MemberService 로 순환이 생김)
 */
@Service
class MemberStatsService(
    private val favoritePersistenceAdapter: FavoritePersistenceAdapter,
    private val reviewPort: ReviewPort,
    private val livePort: LivePort,
) {
    @Transactional(readOnly = true)
    fun getStats(memberId: Long): MemberStats =
        MemberStats(
            reviewCount = reviewPort.countByMemberId(memberId),
            favoriteCount = favoritePersistenceAdapter.countByMemberId(memberId),
            talkCount = livePort.countByMemberId(memberId),
        )
}
