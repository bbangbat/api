package com.bbangbat.common.exception

class BbangbatException(
    val errorCode: ErrorCode,
) : RuntimeException(errorCode.message)
