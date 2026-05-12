package com.bbangbat.store.application.port

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.CONGESTION_UNAVAILABLE
import com.bbangbat.store.domain.CongestionLevel
import org.springframework.stereotype.Component

// TODO: 실시간 서버 연동 시 HTTP 클라이언트로 교체 (재시도 3회, 타임아웃 30초)
@Component
class StubCongestionAdapter : CongestionPort {

    override fun getCongestionLevels(storeIds: List<Long>): Map<Long, CongestionLevel> =
        withRetry { fetchCongestionLevels(storeIds) }

    private fun fetchCongestionLevels(storeIds: List<Long>): Map<Long, CongestionLevel> {
        // TODO: 실시간 서버 HTTP 호출로 교체
        return emptyMap()
    }

    private fun <T> withRetry(block: () -> T): T {
        var lastException: Exception? = null
        repeat(MAX_RETRIES) {
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw BbangbatException(CONGESTION_UNAVAILABLE)
    }

    companion object {
        private const val MAX_RETRIES = 3
    }
}
