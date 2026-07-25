package com.bbangbat.review.repository

import com.bbangbat.review.domain.Review
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "reviews",
    indexes = [
        Index(name = "idx_reviews_store_id", columnList = "store_id"),
        Index(name = "idx_reviews_member_id_id", columnList = "member_id, id"),
    ],
)
class ReviewJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0L,
    @Column(name = "member_id", nullable = false)
    val memberId: Long,
    @Column(name = "store_id", nullable = false)
    val storeId: Long,
    @Column(name = "rating", nullable = false)
    val rating: Int,
    @Column(name = "content", nullable = false, length = 500)
    val content: String,
) : BaseEntity() {
    fun toDomain(
        menus: List<String>,
        imageUrls: List<String>,
    ): Review =
        Review(
            id = id,
            memberId = memberId,
            storeId = storeId,
            rating = rating,
            content = content,
            menus = menus,
            imageUrls = imageUrls,
            createdAt = createdAt,
        )
}
