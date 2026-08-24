package com.bbangbat.member.repository

import com.bbangbat.common.id.Tsid
import com.bbangbat.member.domain.Favorite
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "favorites",
    uniqueConstraints = [UniqueConstraint(name = "uq_favorite_member_store", columnNames = ["member_id", "store_id"])],
    indexes = [Index(name = "idx_favorite_member_id", columnList = "member_id")],
)
class FavoriteJpaEntity(
    @Id
    @Tsid
    @Column(name = "id", nullable = false)
    var id: Long = 0L,
    @Column(name = "member_id", nullable = false)
    var memberId: Long,
    @Column(name = "store_id", nullable = false)
    var storeId: Long,
) : BaseEntity() {
    fun toDomain(): Favorite =
        Favorite(
            id = id,
            memberId = memberId,
            storeId = storeId,
        )

    companion object {
        fun from(favorite: Favorite): FavoriteJpaEntity =
            FavoriteJpaEntity(
                memberId = favorite.memberId,
                storeId = favorite.storeId,
            )
    }
}
