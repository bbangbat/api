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
        val from = LiveTalkMessage.windowStart(LocalDateTime.now())

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
     * 톡을 소프트 삭제한다. 삭제 권한과 삭제 처리 모두 도메인이 판단한다.
     * 삭제된 톡은 목록/집계/요약 대상에서 모두 빠지지만 행은 남는다. (신고 대응 이력 보존)
     */
    @Transactional
    fun deleteMessage(
        messageId: Long,
        memberId: Long,
    ) {
        val message = talkPersistenceAdapter.findMessageById(messageId) ?: throw BbangbatException(TALK_NOT_FOUND)

        if (message.isDeleted) {
            throw BbangbatException(TALK_NOT_FOUND)
        }

        if (!message.canBeDeletedBy(memberId) { memberPort.isAdmin(memberId) }) {
            throw BbangbatException(TALK_FORBIDDEN)
        }

        talkPersistenceAdapter.updateMessage(message.delete(LocalDateTime.now()))
    }

    /**
     * 활성 가게(요약 윈도우 내 톡이 최소 기준 이상) 중
     * 마지막 요약 이후 새 톡이 있는 가게만 AI 요약을 요청한다. 스케줄러가 주기적으로 호출.
     */
    fun summarizeActiveStores() {
        val from = StoreTalkSummary.windowStart(LocalDateTime.now())
        val activeStores = talkPersistenceAdapter.findActiveStores(from, StoreTalkSummary.MIN_MESSAGES)

        activeStores.forEach { active ->
            val existing = talkPersistenceAdapter.findSummaryByStoreId(active.storeId)

            if (existing != null && existing.isUpToDate(active.latestMessageId)) {
                return@forEach
            }

            val messages =
                talkPersistenceAdapter
                    .findRecentMessages(active.storeId, from, null)
                    .takeLast(StoreTalkSummary.MAX_MESSAGES)
                    .map { it.content }

            talkSummaryClient.requestSummary(active.storeId, active.latestMessageId, messages)
        }
    }

    /**
     * AI 서버가 돌려준 요약을 저장한다. 가게당 요약은 하나라 기존 것이 있으면 교체한다.
     */
    @Transactional
    fun saveSummary(
        storeId: Long,
        summary: String,
        lastMessageId: Long,
    ): StoreTalkSummary {
        val existing = talkPersistenceAdapter.findSummaryByStoreId(storeId)

        if (existing != null) {
            return talkPersistenceAdapter.updateSummary(existing.update(summary, lastMessageId))
        }

        return talkPersistenceAdapter.saveSummary(
            StoreTalkSummary(storeId = storeId, summary = summary, lastMessageId = lastMessageId),
        )
    }

    @Transactional(readOnly = true)
    fun getSummaries(storeIds: List<Long>): List<StoreTalkSummary> {
        if (storeIds.isEmpty()) {
            return emptyList()
        }

        return talkPersistenceAdapter.findSummariesByStoreIds(storeIds)
    }
}
