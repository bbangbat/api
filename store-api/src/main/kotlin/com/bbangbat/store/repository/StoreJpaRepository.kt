package com.bbangbat.store.repository

import org.springframework.data.jpa.repository.JpaRepository

interface StoreJpaRepository : JpaRepository<StoreJpaEntity, Long> {
    fun findAllByNameContainingIgnoreCase(name: String): List<StoreJpaEntity>
}
