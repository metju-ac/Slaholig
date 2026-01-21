package org.pv293.kotlinseminar.enrollmentService.infrastructure

import com.thoughtworks.xstream.XStream
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.axonframework.common.jpa.EntityManagerProvider
import org.axonframework.config.Configuration
import org.axonframework.modelling.command.GenericJpaRepository
import org.axonframework.modelling.command.Repository
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.pv293.kotlinseminar.enrollmentService.application.aggregates.AdministrativeAssistant
import org.pv293.kotlinseminar.enrollmentService.application.aggregates.Student
import org.springframework.context.annotation.Bean
import java.util.UUID

@org.springframework.context.annotation.Configuration
class AxonConfig{

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Bean
    fun entityManagerProvider(): EntityManagerProvider {
        return EntityManagerProvider { entityManager }
    }

    @Bean
    fun administrativeAssistantRepository(
        configuration: Configuration,
        entityManagerProvider: EntityManagerProvider
    ): Repository<AdministrativeAssistant> {
        return GenericJpaRepository.builder<AdministrativeAssistant>(AdministrativeAssistant::class.java)
            .identifierConverter { str -> UUID.fromString(str) }
            .entityManagerProvider(entityManagerProvider)
            .eventBus(configuration.eventBus())
            .parameterResolverFactory(configuration.parameterResolverFactory())
            .build()
    }

    @Bean
    fun registeredStudentRepository(
        configuration: Configuration,
        entityManagerProvider: EntityManagerProvider
    ): Repository<Student> {
        return GenericJpaRepository.builder<Student>(Student::class.java)
            .identifierConverter { str -> UUID.fromString(str) }
            .entityManagerProvider(entityManagerProvider)
            .eventBus(configuration.eventBus())
            .parameterResolverFactory(configuration.parameterResolverFactory())
            .build()
    }

    @Bean
    fun eventSerializer(): Serializer {
        val xStream = XStream()
        // Allow all classes from the kotlinseminar packages (all microservices)
        xStream.allowTypesByWildcard(arrayOf(
            "org.pv293.kotlinseminar.**"
        ))
        return XStreamSerializer.builder()
            .xStream(xStream)
            .build()
    }
}