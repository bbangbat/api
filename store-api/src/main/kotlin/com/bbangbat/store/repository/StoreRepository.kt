package com.bbangbat.store.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository

interface StoreRepository :
    JpaRepository<StoreJpaEntity, Long>,
    KotlinJdslJpqlExecutor {
    fun findAllByNameContainingIgnoreCase(name: String): List<StoreJpaEntity>
}
