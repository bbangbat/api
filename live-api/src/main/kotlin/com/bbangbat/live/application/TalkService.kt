package com.bbangbat.live.application

import com.bbangbat.live.client.TalkSummaryClient
import com.bbangbat.live.domain.LiveTalkMessage
import com.bbangbat.live.domain.StoreTalkSummary
import com.bbangbat.live.repository.TalkPersistenceAdapter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TalkService(
    private val talkPersistenceAdapter: TalkPersistenceAdapter,
    private val talkSummaryClient: TalkSummaryClient,
    private val memberPort: MemberPort,
) {
    @Transactional
    fun sendMessage(
        storeId: Long,
        authorId: Long,
        content: String,
    ): LiveTalkMessage {
        val nickname = memberPort.getNickname(authorId)
        val message =
            LiveTalkMessage(
                storeId = storeId,
                authorId = authorId,
                authorNickname = nickname,
                content = content,
                createdAt = LocalDateTime.now(),
            )

        return talkPersistenceAdapter.saveMessage(message)
    }

    @Transactional(readOnly = true)
    fun getMessages(
        storeId: Long,
        afterId: Long?,
    ): List<LiveTalkMessage> {
        val from = LocalDateTime.now().minusHours(MESSAGE_WINDOW_HOURS)

        return talkPersistenceAdapter.findRecentMessages(storeId, from, afterId)
    }

    @Transactional(readOnly = true)
    fun countByAuthorId(authorId: Long): Long = talkPersistenceAdapter.countByAuthorId(authorId)

    /**
     * 활성 가게(최근 SUMMARY_WINDOW_MINUTES 내 톡 MIN_MESSAGES 이상) 중
     * 마지막 요약 이후 새 톡이 있는 가게만 AI 요약을 요청한다. 스케줄러가 주기적으로 호출.
     */
    fun summarizeActiveStores() {
        val from = LocalDateTime.now().minusMinutes(SUMMARY_WINDOW_MINUTES)
        val activeStores = talkPersistenceAdapter.findActiveStores(from, MIN_MESSAGES)

        activeStores.forEach { active ->
            val existing = talkPersistenceAdapter.findSummaryByStoreId(active.storeId)

            if (existing != null && existing.lastMessageId >= active.latestMessageId) {
                return@forEach
            }

            val messages =
                talkPersistenceAdapter
                    .findRecentMessages(active.storeId, from, null)
                    .takeLast(MAX_MESSAGES)
                    .map { it.content }

            talkSummaryClient.requestSummary(active.storeId, active.latestMessageId, messages)
        }
    }

    @Transactional(readOnly = true)
    fun getSummaries(storeIds: List<Long>): List<StoreTalkSummary> {
        if (storeIds.isEmpty()) {
            return emptyList()
        }

        return talkPersistenceAdapter.findSummariesByStoreIds(storeIds)
    }

    companion object {
        private const val MESSAGE_WINDOW_HOURS = 24L
        private const val SUMMARY_WINDOW_MINUTES = 60L
        private const val MIN_MESSAGES = 5L
        private const val MAX_MESSAGES = 100
    }
}
