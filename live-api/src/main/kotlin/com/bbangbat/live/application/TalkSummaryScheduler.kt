package com.bbangbat.live.application

import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 주기적으로 활성 가게의 톡 요약을 갱신한다.
 * SQS·AI 서버가 없는 local 환경에서는 비활성화한다.
 */
@Profile("!local")
@Component
class TalkSummaryScheduler(
    private val talkService: TalkService,
) {
    @Scheduled(fixedRate = SUMMARY_INTERVAL_MS)
    fun refreshSummaries() {
        talkService.summarizeActiveStores()
    }

    companion object {
        private const val SUMMARY_INTERVAL_MS = 5 * 60 * 1000L
    }
}
