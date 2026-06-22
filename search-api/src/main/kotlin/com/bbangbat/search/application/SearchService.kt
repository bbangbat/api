package com.bbangbat.search.application

import com.bbangbat.store.domain.Store
import com.bbangbat.store.repository.StoreRepository
import org.springframework.stereotype.Service

@Service
class SearchService(
    private val storeRepository: StoreRepository,
) {
    fun searchStores(keyword: String): List<Store> = storeRepository.findAllByNameContaining(keyword)
}
