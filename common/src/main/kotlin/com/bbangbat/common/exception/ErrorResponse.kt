package com.bbangbat.common.exception

import com.fasterxml.jackson.annotation.JsonInclude

/** null 필드는 응답에서 제외한다 (retryAfterSeconds는 재시도 가능한 오류에서만 내려간다) */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val code: String,
    val message: String,
    /** 429 등 재시도 가능한 응답에서 다시 시도할 수 있을 때까지 남은 초 */
    val retryAfterSeconds: Long? = null,
)
