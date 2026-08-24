package com.bbangbat.live.application

import com.bbangbat.live.client.SummaryResultMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import tools.jackson.databind.ObjectMapper

/**
 * AI 서버가 결과 큐에 발행한 요약을 폴링으로 수신해 저장한다 (AI 서버 → Spring).
 * SQS·AI 서버가 없는 local 환경에서는 비활성화한다.
 */
@Profile("!local")
@Component
class TalkSummaryResultPoller(
    private val sqsClient: SqsClient,
    private val objectMapper: ObjectMapper,
    private val talkService: TalkService,
    @param:Value("\${cloud.aws.sqs.summary-result-queue-url}") private val resultQueueUrl: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = POLL_INTERVAL_MS)
    fun pollResults() {
        val response =
            sqsClient.receiveMessage(
                ReceiveMessageRequest
                    .builder()
                    .queueUrl(resultQueueUrl)
                    .maxNumberOfMessages(MAX_MESSAGES)
                    .waitTimeSeconds(WAIT_SECONDS)
                    .build(),
            )

        response.messages().forEach { message -> handleMessage(message) }
    }

    private fun handleMessage(message: Message) {
        val result =
            try {
                objectMapper.readValue(message.body(), SummaryResultMessage::class.java)
            } catch (e: Exception) {
                // 파싱 불가한 메시지(poison)는 큐에 남기면 무한 재시도되므로 로그 후 폐기
                log.error("결과 큐 메시지 파싱 실패, 폐기함: body={}", message.body(), e)
                deleteMessage(message)

                return
            }

        try {
            talkService.saveSummary(result.storeId, result.summary, result.lastMessageId)
            deleteMessage(message)
        } catch (e: Exception) {
            // 저장 실패는 일시적일 수 있어 삭제하지 않고 visibility timeout 후 재시도되도록 둔다
            log.error("요약 저장 실패, 재시도 위해 유지: storeId={}", result.storeId, e)
        }
    }

    private fun deleteMessage(message: Message) {
        sqsClient.deleteMessage(
            DeleteMessageRequest
                .builder()
                .queueUrl(resultQueueUrl)
                .receiptHandle(message.receiptHandle())
                .build(),
        )
    }

    companion object {
        private const val POLL_INTERVAL_MS = 10_000L
        private const val MAX_MESSAGES = 10
        private const val WAIT_SECONDS = 5
    }
}
