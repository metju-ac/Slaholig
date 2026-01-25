package org.pv293.kotlinseminar.courierService.infrastructure

import com.thoughtworks.xstream.XStream
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.axonframework.common.jpa.EntityManagerProvider
import org.axonframework.config.Configuration
import org.axonframework.modelling.command.GenericJpaRepository
import org.axonframework.modelling.command.Repository
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.pv293.kotlinseminar.courierService.application.aggregates.CourierQueue
import org.springframework.beans.factory.annotation.Qualifier
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
    fun courierQueueAggregateRepository(
        configuration: Configuration,
        entityManagerProvider: EntityManagerProvider,
    ): Repository<CourierQueue> {
        return GenericJpaRepository.builder<CourierQueue>(CourierQueue::class.java)
            .identifierConverter { str -> UUID.fromString(str) }
            .entityManagerProvider(entityManagerProvider)
            .eventBus(configuration.eventBus())
            .parameterResolverFactory(configuration.parameterResolverFactory())
            .build()
    }

    @Bean
    fun eventSerializer(): Serializer {
        val xStream = XStream()
        xStream.allowTypesByWildcard(
            arrayOf(
                "org.pv293.kotlinseminar.**",
            ),
        )
        return XStreamSerializer.builder()
            .xStream(xStream)
            .build()
    }

    @Bean
    @Qualifier("messageSerializer")
    fun messageSerializer(): Serializer {
        val xStream = XStream()
        xStream.allowTypesByWildcard(
            arrayOf(
                "org.pv293.kotlinseminar.**",
            ),
        )
        return XStreamSerializer.builder()
            .xStream(xStream)
            .build()
    }
}
