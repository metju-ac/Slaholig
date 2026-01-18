package org.pv293.kotlinseminar.coursesService.infrastructure

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.axonframework.common.jpa.EntityManagerProvider
import org.axonframework.config.Configuration
import org.axonframework.modelling.command.GenericJpaRepository
import org.axonframework.modelling.command.Repository
import org.pv293.kotlinseminar.coursesService.application.aggregates.Course
import org.springframework.context.annotation.Bean
import java.util.UUID

@org.springframework.context.annotation.Configuration
class AxonConfig {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Bean
    fun entityManagerProvider(): EntityManagerProvider {
        return EntityManagerProvider { entityManager }
    }


    @Bean
    fun courseRepository(
        configuration: Configuration,
        entityManagerProvider: EntityManagerProvider
    ): Repository<Course> {
        return GenericJpaRepository.builder<Course>(Course::class.java)
            .identifierConverter { str -> UUID.fromString(str) }
            .entityManagerProvider(entityManagerProvider)
            .eventBus(configuration.eventBus())
            .parameterResolverFactory(configuration.parameterResolverFactory())
            .build()
    }
}