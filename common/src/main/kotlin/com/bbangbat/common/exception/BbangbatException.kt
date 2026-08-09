package com.bbangbat.common.exception

class BbangbatException(
    val errorCode: ErrorCode,
    /** 재시도 가능한 오류(429 등)에서 클라이언트가 기다려야 하는 초 */
    val retryAfterSeconds: Long? = null,
) : RuntimeException(errorCode.message)
