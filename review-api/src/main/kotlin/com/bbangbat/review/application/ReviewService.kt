package com.bbangbat.review.application

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.REVIEW_FORBIDDEN
import com.bbangbat.common.exception.ErrorCode.REVIEW_NOT_FOUND
import com.bbangbat.review.domain.Review
import com.bbangbat.review.repository.ReviewRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val s3Service: S3Service,
) {
    fun getReviews(storeId: Long): List<Review> = reviewRepository.findAllByStoreId(storeId)

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

        return reviewRepository.save(review)
    }

    @Transactional
    fun delete(
        memberId: Long,
        reviewId: Long,
    ) {
        val entity =
            reviewRepository.findByIdOrNull(reviewId)
                ?: throw BbangbatException(REVIEW_NOT_FOUND)

        if (entity.memberId != memberId) throw BbangbatException(REVIEW_FORBIDDEN)

        val imageUrls = reviewRepository.getImageUrls(entity)

        reviewRepository.delete(entity)

        imageUrls.forEach { url ->
            val objectKey = url.substringAfter(".amazonaws.com/")
            s3Service.delete(objectKey)
        }
    }

    fun generatePresignedUrls(contentTypes: List<String>): List<PresignedUpload> = contentTypes.map { s3Service.generatePresignedUrl(it) }
}
