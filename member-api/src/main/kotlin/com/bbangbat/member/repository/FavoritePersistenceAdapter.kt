package com.bbangbat.member.repository

import com.bbangbat.member.domain.Favorite
import org.springframework.stereotype.Repository

@Repository
class FavoritePersistenceAdapter(
    private val favoriteRepository: FavoriteRepository,
) {
    fun existsByMemberIdAndStoreId(
        memberId: Long,
        storeId: Long,
    ): Boolean = favoriteRepository.existsByMemberIdAndStoreId(memberId, storeId)

    fun findByMemberIdAndStoreId(
        memberId: Long,
        storeId: Long,
    ): Favorite? =
        favoriteRepository
            .findByMemberIdAndStoreId(memberId, storeId)
            .orElse(null)
            ?.toDomain()

    fun findAllStoreIdsByMemberId(memberId: Long): List<Long> =
        favoriteRepository.findAllByMemberIdOrderByIdDesc(memberId).map { it.storeId }

    fun countByMemberId(memberId: Long): Long = favoriteRepository.countByMemberId(memberId)

    fun deleteAllByMemberId(memberId: Long) = favoriteRepository.deleteAllByMemberId(memberId)

    fun save(favorite: Favorite): Favorite = favoriteRepository.save(FavoriteJpaEntity.from(favorite)).toDomain()

    fun delete(favorite: Favorite) {
        favoriteRepository.deleteById(favorite.id)
    }
}
