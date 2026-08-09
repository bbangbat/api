package com.bbangbat.live.application

import com.bbangbat.live.repository.TalkPersistenceAdapter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 다른 모듈에 노출하는 톡 통계 조회.
 *
 * [TalkService]는 닉네임 스냅샷 때문에 [MemberPort]를 의존해서, 회원 쪽에서 이 서비스를 호출하면
 * member -> live -> member 순환 참조가 된다. 회원 정보가 필요 없는 집계만 따로 떼어 둔다.
 */
@Service
class TalkStatsService(
    private val talkPersistenceAdapter: TalkPersistenceAdapter,
) {
    @Transactional(readOnly = true)
    fun countByAuthorId(authorId: Long): Long = talkPersistenceAdapter.countByAuthorId(authorId)
}
