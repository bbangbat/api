package com.bbangbat.member.application

import com.bbangbat.common.exception.BbangbatException
import com.bbangbat.common.exception.ErrorCode.FAVORITE_ALREADY_EXISTS
import com.bbangbat.common.exception.ErrorCode.FAVORITE_NOT_FOUND
import com.bbangbat.member.domain.Favorite
import com.bbangbat.member.repository.FavoriteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FavoriteService(
    private val favoriteRepository: FavoriteRepository,
) {
    @Transactional
    fun add(
        memberId: Long,
        storeId: Long,
    ) {
        if (favoriteRepository.existsByMemberIdAndStoreId(memberId, storeId)) {
            throw BbangbatException(FAVORITE_ALREADY_EXISTS)
        }

        favoriteRepository.save(Favorite(memberId = memberId, storeId = storeId))
    }

    @Transactional
    fun remove(
        memberId: Long,
        storeId: Long,
    ) {
        val favorite =
            favoriteRepository.findByMemberIdAndStoreId(memberId, storeId)
                ?: throw BbangbatException(FAVORITE_NOT_FOUND)

        favoriteRepository.delete(favorite)
    }

    @Transactional(readOnly = true)
    fun findStoreIds(memberId: Long): List<Long> = favoriteRepository.findAllStoreIdsByMemberId(memberId)
}
