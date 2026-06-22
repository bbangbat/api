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
    indexes = [Index(name = "idx_reviews_store_id", columnList = "store_id")],
)
class ReviewJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(name = "member_id", nullable = false)
    val memberId: Long,
    @Column(name = "store_id", nullable = false)
    val storeId: Long,
    @Column(nullable = false)
    val rating: Int,
    @Column(nullable = false, length = 500)
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
