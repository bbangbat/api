package com.bbangbat.review.application

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.REVIEW_FORBIDDEN
import com.bbangbat.common.exception.ErrorCode.REVIEW_NOT_FOUND
import com.bbangbat.review.domain.Review
import com.bbangbat.review.repository.ReviewJpaEntity
import com.bbangbat.review.repository.ReviewPersistenceAdapter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class ReviewServiceTest {
    @Mock
    private lateinit var reviewPersistenceAdapter: ReviewPersistenceAdapter

    @Mock
    private lateinit var s3Service: S3Service

    @Mock
    private lateinit var storePort: StorePort

    @InjectMocks
    private lateinit var reviewService: ReviewService

    @Test
    fun `회원이 작성한 리뷰 수를 조회한다`() {
        whenever(reviewPersistenceAdapter.countByMemberId(1L)).thenReturn(3L)

        val result = reviewService.countByMemberId(1L)

        assertEquals(3L, result)
    }

    @Test
    fun `내 리뷰를 가게 정보와 함께 조회한다`() {
        val memberId = 1L
        val storeId = 2L
        val review =
            Review(
                id = 3L,
                memberId = memberId,
                storeId = storeId,
                rating = 5,
                content = "정말 맛있는 빵집이라 추천합니다",
                menus = listOf("소금빵"),
                imageUrls = listOf("https://example.com/review.jpg"),
            )
        val store =
            ReviewStore(
                id = storeId,
                name = "빵빵 베이커리",
                imageUrl = "https://example.com/store.jpg",
            )

        whenever(reviewPersistenceAdapter.findAllByMemberId(memberId)).thenReturn(listOf(review))
        whenever(storePort.findByIds(setOf(storeId))).thenReturn(mapOf(storeId to store))

        val result = reviewService.getMyReviews(memberId)

        assertEquals(1, result.size)
        assertEquals(review, result[0].review)
        assertEquals(store, result[0].store)
    }

    @Test
    fun `리뷰 작성 시 이미지 키를 S3 URL로 변환하여 저장한다`() {
        val memberId = 1L
        val storeId = 2L
        val imageKeys = listOf("reviews/uuid1", "reviews/uuid2")
        val imageUrls = imageKeys.map { "https://bucket.s3.ap-northeast-2.amazonaws.com/$it" }
        val savedReview =
            Review(
                id = 1L,
                memberId = memberId,
                storeId = storeId,
                rating = 5,
                content = "맛있는 빵집입니다. 강추!",
                menus = listOf("소금빵", "크루아상"),
                imageUrls = imageUrls,
            )

        whenever(s3Service.buildUrl("reviews/uuid1")).thenReturn(imageUrls[0])
        whenever(s3Service.buildUrl("reviews/uuid2")).thenReturn(imageUrls[1])
        whenever(reviewPersistenceAdapter.save(any())).thenReturn(savedReview)

        val result =
            reviewService.create(
                memberId = memberId,
                storeId = storeId,
                rating = 5,
                content = "맛있는 빵집입니다. 강추!",
                menus = listOf("소금빵", "크루아상"),
                imageKeys = imageKeys,
            )

        assertEquals(savedReview.id, result.id)
        assertEquals(imageUrls, result.imageUrls)
    }

    @Test
    fun `이미지 없이 리뷰를 작성할 수 있다`() {
        val memberId = 1L
        val storeId = 2L
        val savedReview =
            Review(
                id = 1L,
                memberId = memberId,
                storeId = storeId,
                rating = 4,
                content = "괜찮은 빵집입니다 추천해요",
                menus = listOf("소금빵"),
                imageUrls = emptyList(),
            )

        whenever(reviewPersistenceAdapter.save(any())).thenReturn(savedReview)

        val result =
            reviewService.create(
                memberId = memberId,
                storeId = storeId,
                rating = 4,
                content = "괜찮은 빵집입니다 추천해요",
                menus = listOf("소금빵"),
                imageKeys = emptyList(),
            )

        assertEquals(emptyList<String>(), result.imageUrls)
        verify(s3Service, never()).buildUrl(any())
    }

    @Test
    fun `존재하지 않는 리뷰 삭제 시 예외가 발생한다`() {
        whenever(reviewPersistenceAdapter.findByIdOrNull(99L)).thenReturn(null)

        val exception =
            assertThrows(BbangbatException::class.java) {
                reviewService.delete(memberId = 1L, reviewId = 99L)
            }

        assertEquals(REVIEW_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `다른 회원의 리뷰 삭제 시 예외가 발생한다`() {
        val entity = ReviewJpaEntity(id = 1L, memberId = 1L, storeId = 2L, rating = 5, content = "맛있어요테스트용입니다")

        whenever(reviewPersistenceAdapter.findByIdOrNull(1L)).thenReturn(entity)

        val exception =
            assertThrows(BbangbatException::class.java) {
                reviewService.delete(memberId = 999L, reviewId = 1L)
            }

        assertEquals(REVIEW_FORBIDDEN, exception.errorCode)
    }

    @Test
    fun `리뷰 삭제 시 S3 이미지도 함께 삭제된다`() {
        val memberId = 1L
        val entity = ReviewJpaEntity(id = 1L, memberId = memberId, storeId = 2L, rating = 5, content = "맛있어요테스트용입니다")
        val imageUrls =
            listOf(
                "https://bucket.s3.ap-northeast-2.amazonaws.com/reviews/uuid1",
                "https://bucket.s3.ap-northeast-2.amazonaws.com/reviews/uuid2",
            )

        whenever(reviewPersistenceAdapter.findByIdOrNull(1L)).thenReturn(entity)
        whenever(reviewPersistenceAdapter.getImageUrls(entity)).thenReturn(imageUrls)

        reviewService.delete(memberId = memberId, reviewId = 1L)

        verify(reviewPersistenceAdapter).delete(entity)
        verify(s3Service).delete("reviews/uuid1")
        verify(s3Service).delete("reviews/uuid2")
    }

    @Test
    fun `이미지 없는 리뷰 삭제 시 S3 삭제를 호출하지 않는다`() {
        val memberId = 1L
        val entity = ReviewJpaEntity(id = 1L, memberId = memberId, storeId = 2L, rating = 5, content = "맛있어요테스트용입니다")

        whenever(reviewPersistenceAdapter.findByIdOrNull(1L)).thenReturn(entity)
        whenever(reviewPersistenceAdapter.getImageUrls(entity)).thenReturn(emptyList())

        reviewService.delete(memberId = memberId, reviewId = 1L)

        verify(reviewPersistenceAdapter).delete(entity)
        verify(s3Service, never()).delete(any())
    }
}
