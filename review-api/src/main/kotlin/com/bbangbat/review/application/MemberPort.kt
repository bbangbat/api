package com.bbangbat.review.application

/**
 * 리뷰 작성자 정보 조회 포트.
 * review-api는 member-api를 의존하지 않으므로 구현(어댑터)은 app 모듈에 둔다.
 */
interface MemberPort {
    /** 존재하는 회원의 정보만 반환한다. (탈퇴한 회원은 결과에 없음) */
    fun findAuthors(memberIds: Collection<Long>): Map<Long, ReviewAuthor>
}

data class ReviewAuthor(
    val nickname: String,
    val profileImageUrl: String?,
)
