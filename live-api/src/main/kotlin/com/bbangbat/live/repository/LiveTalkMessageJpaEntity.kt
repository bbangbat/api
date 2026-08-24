package com.bbangbat.live.repository

import com.bbangbat.live.domain.LiveTalkMessage
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "live_talk_messages",
    indexes = [
        Index(name = "idx_talk_store_created_at", columnList = "store_id, created_at"),
        Index(name = "idx_talk_author_id", columnList = "author_id"),
    ],
)
class LiveTalkMessageJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long = 0L,
    @Column(name = "store_id", nullable = false)
    var storeId: Long,
    @Column(name = "author_id", nullable = false)
    var authorId: Long,
    @Column(name = "author_nickname", nullable = false, length = LiveTalkMessage.MAX_AUTHOR_NICKNAME_LENGTH)
    var authorNickname: String,
    @Column(name = "content", nullable = false, length = LiveTalkMessage.MAX_CONTENT_LENGTH)
    var content: String,
    /** 소프트 삭제 시각. null이면 살아 있는 메시지. 조회는 모두 이 값이 null인 것만 대상으로 한다. */
    @Column(name = "deleted_at", nullable = true)
    var deletedAt: LocalDateTime? = null,
) : BaseEntity() {
    /**
     * 도메인 상태를 영속 엔티티에 반영한다. (더티체킹)
     * 어떤 필드가 바뀔 수 있는지는 도메인이 결정하므로 여기서는 상태를 그대로 옮겨 담는다.
     */
    fun applyFrom(message: LiveTalkMessage) {
        storeId = message.storeId
        authorId = message.authorId
        authorNickname = message.authorNickname
        content = message.content
        deletedAt = message.deletedAt
    }

    fun toDomain(): LiveTalkMessage =
        LiveTalkMessage(
            id = id,
            storeId = storeId,
            authorId = authorId,
            authorNickname = authorNickname,
            content = content,
            createdAt = requireNotNull(createdAt) { "영속화되지 않은 엔티티입니다." },
            deletedAt = deletedAt,
        )

    companion object {
        fun from(message: LiveTalkMessage): LiveTalkMessageJpaEntity =
            LiveTalkMessageJpaEntity(
                storeId = message.storeId,
                authorId = message.authorId,
                authorNickname = message.authorNickname,
                content = message.content,
                deletedAt = message.deletedAt,
            )
    }
}
