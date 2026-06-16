package com.bbangbat.live.application

import com.bbangbat.live.domain.LiveTalkMessage
import com.bbangbat.live.repository.LiveTalkMessageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class LiveTalkService(
    private val liveTalkMessageRepository: LiveTalkMessageRepository,
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

        return liveTalkMessageRepository.save(message)
    }

    @Transactional(readOnly = true)
    fun getMessages(
        storeId: Long,
        afterId: Long?,
    ): List<LiveTalkMessage> {
        val from = LocalDateTime.now().minusHours(WINDOW_HOURS)

        return liveTalkMessageRepository.findRecentMessages(storeId, from, afterId)
    }

    companion object {
        private const val WINDOW_HOURS = 24L
    }
}
