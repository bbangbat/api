package com.bbangbat.review.repository

import com.bbangbat.review.domain.Review
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ReviewPersistenceAdapter(
    private val reviewRepository: ReviewRepository,
    private val reviewImageRepository: ReviewImageRepository,
    private val reviewMenuRepository: ReviewMenuRepository,
) {
    fun findByIdOrNull(id: Long): ReviewJpaEntity? = reviewRepository.findById(id).orElse(null)

    fun findAllByStoreId(storeId: Long): List<Review> {
        val entities = reviewRepository.findAllByStoreIdOrderByIdDesc(storeId)

        if (entities.isEmpty()) return emptyList()

        val images =
            reviewImageRepository
                .findAllByReviewInOrderByDisplayOrder(entities)
                .groupBy { it.review.id }
        val menus =
            reviewMenuRepository
                .findAllByReviewIn(entities)
                .groupBy { it.review.id }

        return entities.map { entity ->
            entity.toDomain(
                menus = menus[entity.id]?.map { it.menuName } ?: emptyList(),
                imageUrls = images[entity.id]?.map { it.imageUrl } ?: emptyList(),
            )
        }
    }

    @Transactional
    fun save(review: Review): Review {
        val entity =
            reviewRepository.save(
                ReviewJpaEntity(
                    memberId = review.memberId,
                    storeId = review.storeId,
                    rating = review.rating,
                    content = review.content,
                ),
            )
        review.imageUrls.forEachIndexed { index, url ->
            reviewImageRepository.save(ReviewImageJpaEntity(review = entity, imageUrl = url, displayOrder = index))
        }
        review.menus.forEach { menu ->
            reviewMenuRepository.save(ReviewMenuJpaEntity(review = entity, menuName = menu))
        }

        return entity.toDomain(menus = review.menus, imageUrls = review.imageUrls)
    }

    @Transactional
    fun delete(entity: ReviewJpaEntity) {
        reviewMenuRepository.deleteAllByReview(entity)
        reviewImageRepository.deleteAllByReview(entity)
        reviewRepository.delete(entity)
    }

    fun getImageUrls(entity: ReviewJpaEntity): List<String> =
        reviewImageRepository
            .findAllByReviewInOrderByDisplayOrder(listOf(entity))
            .map { it.imageUrl }
}
