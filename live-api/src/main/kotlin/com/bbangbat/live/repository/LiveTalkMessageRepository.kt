package com.bbangbat.live.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository

interface LiveTalkMessageRepository :
    JpaRepository<LiveTalkMessageJpaEntity, Long>,
    KotlinJdslJpqlExecutor
