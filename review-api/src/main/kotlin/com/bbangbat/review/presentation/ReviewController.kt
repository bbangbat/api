package com.bbangbat.review.presentation

import com.bbangbat.auth.resolver.AuthMember
import com.bbangbat.review.application.ReviewService
import com.bbangbat.review.presentation.dto.CreateReviewRequest
import com.bbangbat.review.presentation.dto.PresignedUrlRequest
import com.bbangbat.review.presentation.dto.PresignedUrlResponse
import com.bbangbat.review.presentation.dto.ReviewResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "빵명록", description = "빵명록(리뷰) API")
@RestController
@RequestMapping("/api/reviews")
class ReviewController(
    private val reviewService: ReviewService,
) {
    @Operation(
        summary = "S3 Presigned URL 발급",
        description = "이미지 업로드용 Presigned URL을 발급합니다. 발급 후 5분 내에 S3에 직접 업로드해야 합니다. 회원 전용.",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "발급 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 이미지 형식 또는 5장 초과"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
    )
    @PostMapping("/presigned-urls")
    @ResponseStatus(OK)
    fun getPresignedUrls(
        @RequestBody @Valid request: PresignedUrlRequest,
    ): List<PresignedUrlResponse> {
        val uploads = reviewService.generatePresignedUrls(request.contentTypes)

        return uploads.map { PresignedUrlResponse.from(it) }
    }

    @Operation(summary = "빵명록 목록 조회", description = "특정 가게의 빵명록 목록을 최신순으로 조회합니다. 비회원도 가능합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
    )
    @GetMapping
    @ResponseStatus(OK)
    fun getReviews(
        @RequestParam storeId: Long,
    ): List<ReviewResponse> {
        val reviews = reviewService.getReviews(storeId)

        return reviews.map { ReviewResponse.from(it) }
    }

    @Operation(summary = "빵명록 작성", description = "특정 가게에 빵명록을 작성합니다. 회원 전용.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "작성 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 입력값 (별점 범위, 후기 길이, 메뉴 누락 등)"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
    )
    @PostMapping
    @ResponseStatus(CREATED)
    fun createReview(
        @AuthMember memberId: Long,
        @RequestBody @Valid request: CreateReviewRequest,
    ): ReviewResponse {
        val review =
            reviewService.create(
                memberId = memberId,
                storeId = request.storeId,
                rating = request.rating,
                content = request.content,
                menus = request.menus,
                imageKeys = request.imageKeys,
            )

        return ReviewResponse.from(review)
    }

    @Operation(summary = "빵명록 삭제", description = "자신이 작성한 빵명록을 삭제합니다. 회원 전용.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요"),
        ApiResponse(responseCode = "403", description = "본인 리뷰가 아님"),
        ApiResponse(responseCode = "404", description = "리뷰 없음"),
    )
    @DeleteMapping("/{reviewId}")
    @ResponseStatus(NO_CONTENT)
    fun deleteReview(
        @AuthMember memberId: Long,
        @PathVariable reviewId: Long,
    ) {
        reviewService.delete(memberId, reviewId)
    }
}
