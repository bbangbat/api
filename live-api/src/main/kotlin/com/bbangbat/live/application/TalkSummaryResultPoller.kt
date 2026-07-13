package com.bbangbat.live.application

import com.bbangbat.live.client.SummaryResultMessage
import com.bbangbat.live.domain.StoreTalkSummary
import com.bbangbat.live.repository.TalkPersistenceAdapter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
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
    private val talkPersistenceAdapter: TalkPersistenceAdapter,
    @param:Value("\${cloud.aws.sqs.summary-result-queue-url}") private val resultQueueUrl: String,
) {
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

        response.messages().forEach { message ->
            val result = objectMapper.readValue(message.body(), SummaryResultMessage::class.java)

            talkPersistenceAdapter.upsertSummary(
                StoreTalkSummary(
                    storeId = result.storeId,
                    summary = result.summary,
                    lastMessageId = result.lastMessageId,
                ),
            )

            sqsClient.deleteMessage(
                DeleteMessageRequest
                    .builder()
                    .queueUrl(resultQueueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build(),
            )
        }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 10_000L
        private const val MAX_MESSAGES = 10
        private const val WAIT_SECONDS = 5
    }
}
