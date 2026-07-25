package com.bbangbat.review.application

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.REVIEW_FORBIDDEN
import com.bbangbat.common.exception.ErrorCode.REVIEW_NOT_FOUND
import com.bbangbat.review.domain.Review
import com.bbangbat.review.repository.ReviewPersistenceAdapter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewService(
    private val reviewPersistenceAdapter: ReviewPersistenceAdapter,
    private val s3Service: S3Service,
    private val storePort: StorePort,
) {
    fun getReviews(storeId: Long): List<Review> = reviewPersistenceAdapter.findAllByStoreId(storeId)

    fun getMyReviews(memberId: Long): List<MyReview> {
        val reviews = reviewPersistenceAdapter.findAllByMemberId(memberId)

        val storesById = storePort.findByIds(reviews.map { it.storeId }.toSet())

        return reviews.map { review ->
            val store =
                checkNotNull(storesById[review.storeId]) {
                    "리뷰에 연결된 가게를 찾을 수 없습니다. storeId=${review.storeId}"
                }
            MyReview(review = review, store = store)
        }
    }

    @Transactional(readOnly = true)
    fun countByMemberId(memberId: Long): Long = reviewPersistenceAdapter.countByMemberId(memberId)

    @Transactional
    fun create(
        memberId: Long,
        storeId: Long,
        rating: Int,
        content: String,
        menus: List<String>,
        imageKeys: List<String>,
    ): Review {
        val review =
            Review(
                memberId = memberId,
                storeId = storeId,
                rating = rating,
                content = content,
                menus = menus,
                imageUrls = imageKeys.map { s3Service.buildUrl(it) },
            )

        return reviewPersistenceAdapter.save(review)
    }

    @Transactional
    fun delete(
        memberId: Long,
        reviewId: Long,
    ) {
        val entity =
            reviewPersistenceAdapter.findByIdOrNull(reviewId)
                ?: throw BbangbatException(REVIEW_NOT_FOUND)

        if (entity.memberId != memberId) throw BbangbatException(REVIEW_FORBIDDEN)

        val imageUrls = reviewPersistenceAdapter.getImageUrls(entity)

        reviewPersistenceAdapter.delete(entity)

        imageUrls.forEach { url ->
            val objectKey = url.substringAfter(".amazonaws.com/")
            s3Service.delete(objectKey)
        }
    }

    fun generatePresignedUrls(contentTypes: List<String>): List<PresignedUpload> = contentTypes.map { s3Service.generatePresignedUrl(it) }
}
