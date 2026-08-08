package com.bbangbat.member.domain

/**
 * 닉네임 정책. 요청 DTO 검증과 도메인 검증이 같은 규칙을 공유한다.
 *
 * 완성형 한글/영문/숫자만 허용하므로 자음/모음 단독(ㄱ, ㅏ 등)과 공백은 자연히 걸러진다.
 */
object NicknamePolicy {
    const val MIN_LENGTH = 2
    const val MAX_LENGTH = 10
    const val REGEX = "^[가-힣a-zA-Z0-9]+$"

    const val LENGTH_MESSAGE = "닉네임은 ${MIN_LENGTH}자 이상 ${MAX_LENGTH}자 이하여야 합니다."
    const val FORMAT_MESSAGE = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다. (자음/모음 단독, 공백 불가)"

    private val PATTERN = Regex(REGEX)

    fun isValidFormat(nickname: String): Boolean = PATTERN.matches(nickname)
}
