package com.bbangbat.live.api

import com.bbangbat.auth.resolver.AuthMember
import com.bbangbat.common.exception.ErrorResponse
import com.bbangbat.live.api.dto.LiveTalkMessageRequest
import com.bbangbat.live.api.dto.LiveTalkMessageResponse
import com.bbangbat.live.api.dto.StoreTalkSummaryResponse
import com.bbangbat.live.application.TalkService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "실시간 톡", description = "가게별 실시간 톡 API (조회는 비회원도 가능, 작성은 회원 전용)")
@RestController
@RequestMapping("/api/talks")
class TalkController(
    private val talkService: TalkService,
) {
    @Operation(summary = "실시간 톡 전송", description = "회원만 가능합니다. 메시지는 최대 100자입니다.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "전송 성공"),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (메시지 누락/길이 초과)",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(value = "{\"code\": \"INVALID_INPUT\", \"message\": \"메시지는 100자를 초과할 수 없습니다.\"}")],
                ),
            ],
        ),
        ApiResponse(
            responseCode = "401",
            description = "인증 필요 (비회원 불가)",
            content = [
                Content(
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(value = "{\"code\": \"UNAUTHORIZED\", \"message\": \"인증이 필요합니다.\"}")],
                ),
            ],
        ),
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun sendMessage(
        @RequestBody @Valid request: LiveTalkMessageRequest,
        @AuthMember authorId: Long,
    ): LiveTalkMessageResponse =
        LiveTalkMessageResponse.from(
            talkService.sendMessage(
                storeId = request.storeId!!,
                authorId = authorId,
                content = request.content!!,
            ),
        )

    @Operation(
        summary = "실시간 톡 조회",
        description =
            "가게의 최근 24시간 내 메시지를 조회합니다. 비회원도 가능합니다. " +
                "afterId를 전달하면 그 이후에 작성된 메시지만 반환합니다 (폴링용).",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getMessages(
        @RequestParam storeId: Long,
        @Parameter(description = "이 ID 이후 메시지만 조회 (최초 조회 시 생략)", example = "10")
        @RequestParam(required = false) afterId: Long?,
    ): List<LiveTalkMessageResponse> = talkService.getMessages(storeId, afterId).map { LiveTalkMessageResponse.from(it) }

    @Operation(
        summary = "톡 요약 벌크 조회",
        description = "여러 가게의 실시간 톡 한 줄 요약을 조회합니다. 요약이 없는 가게는 결과에서 제외됩니다. 비회원도 가능합니다.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
    )
    @GetMapping("/summary")
    @ResponseStatus(HttpStatus.OK)
    fun getSummaries(
        @RequestParam storeIds: List<Long>,
    ): List<StoreTalkSummaryResponse> = talkService.getSummaries(storeIds).map { StoreTalkSummaryResponse.from(it) }
}
