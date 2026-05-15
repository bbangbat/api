package com.bbangbat.store.support

import com.linecorp.kotlinjdsl.render.RenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutorImpl
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class JdslTestConfiguration {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Bean
    fun jpqlRenderContext(): JpqlRenderContext = JpqlRenderContext()

    @Bean
    fun kotlinJdslJpqlExecutor(renderContexts: List<RenderContext>): KotlinJdslJpqlExecutor {
        val renderContext = renderContexts.reversed().reduce { acc, ctx -> acc + ctx }
        return KotlinJdslJpqlExecutorImpl(entityManager, renderContext, null)
    }
}
