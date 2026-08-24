package com.bbangbat.live.domain

import java.time.LocalDateTime

data class LiveTalkMessage(
    val id: Long = 0L,
    val storeId: Long,
    val authorId: Long,
    val authorNickname: String,
    val content: String,
    val createdAt: LocalDateTime,
    /** 소프트 삭제 시각. null이면 살아 있는 메시지. */
    val deletedAt: LocalDateTime? = null,
) {
    init {
        require(storeId > 0) { "가게 ID가 올바르지 않습니다." }
        require(content.isNotBlank()) { "메시지는 비어 있을 수 없습니다." }
        require(content.length <= MAX_CONTENT_LENGTH) { "메시지는 ${MAX_CONTENT_LENGTH}자를 초과할 수 없습니다." }
    }

    val isDeleted: Boolean
        get() = deletedAt != null

    /**
     * 삭제 권한. 작성자 본인이거나 운영자만 삭제할 수 있다.
     * 운영자 여부는 회원 도메인 소관이라 지연 조회로 받는다. (작성자 본인이면 조회하지 않는다)
     */
    fun canBeDeletedBy(
        memberId: Long,
        isAdmin: () -> Boolean,
    ): Boolean = authorId == memberId || isAdmin()

    /**
     * 소프트 삭제. 행은 남기고 삭제 시각만 채운다. (신고 대응 이력 보존)
     * 삭제된 메시지는 목록/집계/요약 대상에서 모두 빠진다.
     */
    fun delete(at: LocalDateTime): LiveTalkMessage = copy(deletedAt = at)

    companion object {
        const val MAX_CONTENT_LENGTH = 100

        /**
         * 작성자 닉네임 스냅샷의 최대 길이. member-api의 NicknamePolicy.MAX_LENGTH와 같은 값이다.
         * 모듈 간 도메인 타입을 공유하지 않는 원칙 때문에 상수를 참조하지 않고 값만 맞춘다.
         * 읽기 시점에는 검증하지 않는다 — 정책 축소 이전에 저장된 스냅샷이 남아 있을 수 있다.
         */
        const val MAX_AUTHOR_NICKNAME_LENGTH = 10

        /** 조회 윈도우. 이 시간 내에 작성된 메시지만 노출한다. */
        const val WINDOW_HOURS = 24L

        /** 조회 대상이 되는 가장 이른 작성 시각 */
        fun windowStart(now: LocalDateTime): LocalDateTime = now.minusHours(WINDOW_HOURS)
    }
}
