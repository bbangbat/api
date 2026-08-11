package com.bbangbat.live.application

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.TALK_FORBIDDEN
import com.bbangbat.common.exception.ErrorCode.TALK_NOT_FOUND
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
    private val storePort: StorePort,
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

    /** 내가 쓴 톡 목록 (최신순). 삭제한 톡은 제외하고, 가게명은 일괄 조회로 붙인다. */
    @Transactional(readOnly = true)
    fun getMyMessages(authorId: Long): List<MyTalkMessage> {
        val messages = talkPersistenceAdapter.findByAuthorId(authorId)
        val storeNames = storePort.findNames(messages.map { it.storeId }.distinct())

        return messages.map { MyTalkMessage.of(it, storeNames[it.storeId]) }
    }

    /**
     * 톡을 소프트 삭제한다. 작성자 본인이거나 운영자만 가능하다.
     * 삭제된 톡은 목록/집계/요약 대상에서 모두 빠지지만 행은 남는다. (신고 대응 이력 보존)
     */
    @Transactional
    fun deleteMessage(
        messageId: Long,
        memberId: Long,
    ) {
        val message = talkPersistenceAdapter.findMessageById(messageId) ?: throw BbangbatException(TALK_NOT_FOUND)

        if (message.authorId != memberId && !memberPort.isAdmin(memberId)) {
            throw BbangbatException(TALK_FORBIDDEN)
        }

        talkPersistenceAdapter.softDeleteMessage(messageId, LocalDateTime.now())
    }

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
