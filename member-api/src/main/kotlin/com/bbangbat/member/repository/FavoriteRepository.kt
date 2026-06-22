package com.bbangbat.member.repository

import com.bbangbat.member.domain.Favorite
import org.springframework.stereotype.Repository

@Repository
class FavoriteRepository(
    private val favoriteJpaRepository: FavoriteJpaRepository,
) {
    fun existsByMemberIdAndStoreId(
        memberId: Long,
        storeId: Long,
    ): Boolean = favoriteJpaRepository.existsByMemberIdAndStoreId(memberId, storeId)

    fun findByMemberIdAndStoreId(
        memberId: Long,
        storeId: Long,
    ): Favorite? =
        favoriteJpaRepository
            .findByMemberIdAndStoreId(memberId, storeId)
            .orElse(null)
            ?.toDomain()

    fun findAllStoreIdsByMemberId(memberId: Long): List<Long> = favoriteJpaRepository.findAllByMemberId(memberId).map { it.storeId }

    fun save(favorite: Favorite): Favorite = favoriteJpaRepository.save(FavoriteJpaEntity.from(favorite)).toDomain()

    fun delete(favorite: Favorite) {
        favoriteJpaRepository.deleteById(favorite.id)
    }
}
