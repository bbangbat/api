package com.bbangbat.live.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import tools.jackson.databind.ObjectMapper

/**
 * 톡 요약 요청을 SQS 요청 큐로 발행한다 (Spring → AI 서버, 비동기).
 * 결과는 TalkSummaryResultPoller가 결과 큐에서 수신해 저장한다.
 */
@Component
class TalkSummaryClient(
    private val sqsClient: SqsClient,
    private val objectMapper: ObjectMapper,
    @param:Value("\${cloud.aws.sqs.summary-request-queue-url:}") private val requestQueueUrl: String,
) {
    fun requestSummary(
        storeId: Long,
        lastMessageId: Long,
        messages: List<String>,
    ) {
        val body = objectMapper.writeValueAsString(SummaryRequestMessage(storeId, lastMessageId, messages))

        sqsClient.sendMessage(
            SendMessageRequest
                .builder()
                .queueUrl(requestQueueUrl)
                .messageBody(body)
                .build(),
        )
    }
}
