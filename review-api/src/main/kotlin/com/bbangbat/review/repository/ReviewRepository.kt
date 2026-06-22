package com.bbangbat.review.repository

import com.bbangbat.review.domain.Review
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ReviewRepository(
    private val reviewJpaRepository: ReviewJpaRepository,
    private val reviewImageJpaRepository: ReviewImageJpaRepository,
    private val reviewMenuJpaRepository: ReviewMenuJpaRepository,
) {
    fun findByIdOrNull(id: Long): ReviewJpaEntity? = reviewJpaRepository.findById(id).orElse(null)

    fun findAllByStoreId(storeId: Long): List<Review> {
        val entities = reviewJpaRepository.findAllByStoreIdOrderByIdDesc(storeId)

        if (entities.isEmpty()) return emptyList()

        val images =
            reviewImageJpaRepository
                .findAllByReviewInOrderByDisplayOrder(entities)
                .groupBy { it.review.id }
        val menus =
            reviewMenuJpaRepository
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
            reviewJpaRepository.save(
                ReviewJpaEntity(
                    memberId = review.memberId,
                    storeId = review.storeId,
                    rating = review.rating,
                    content = review.content,
                ),
            )
        review.imageUrls.forEachIndexed { index, url ->
            reviewImageJpaRepository.save(ReviewImageJpaEntity(review = entity, imageUrl = url, displayOrder = index))
        }
        review.menus.forEach { menu ->
            reviewMenuJpaRepository.save(ReviewMenuJpaEntity(review = entity, menuName = menu))
        }

        return entity.toDomain(menus = review.menus, imageUrls = review.imageUrls)
    }

    @Transactional
    fun delete(entity: ReviewJpaEntity) {
        reviewMenuJpaRepository.deleteAllByReview(entity)
        reviewImageJpaRepository.deleteAllByReview(entity)
        reviewJpaRepository.delete(entity)
    }

    fun getImageUrls(entity: ReviewJpaEntity): List<String> =
        reviewImageJpaRepository
            .findAllByReviewInOrderByDisplayOrder(listOf(entity))
            .map { it.imageUrl }
}
