package org.pv293.kotlinseminar.productSelectionService.infrastructure

import com.thoughtworks.xstream.XStream
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.axonframework.common.jpa.EntityManagerProvider
import org.axonframework.config.Configuration
import org.axonframework.modelling.command.GenericJpaRepository
import org.axonframework.modelling.command.Repository
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.pv293.kotlinseminar.productSelectionService.application.aggregates.BakedGood
import org.pv293.kotlinseminar.productSelectionService.application.aggregates.ChosenLocation
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
    fun bakedGoodAggregateRepository(
        configuration: Configuration,
        entityManagerProvider: EntityManagerProvider,
    ): Repository<BakedGood> {
        return GenericJpaRepository.builder<BakedGood>(BakedGood::class.java)
            .identifierConverter { str -> UUID.fromString(str) }
            .entityManagerProvider(entityManagerProvider)
            .eventBus(configuration.eventBus())
            .parameterResolverFactory(configuration.parameterResolverFactory())
            .build()
    }

    @Bean
    fun chosenLocationAggregateRepository(
        configuration: Configuration,
        entityManagerProvider: EntityManagerProvider,
    ): Repository<ChosenLocation> {
        return GenericJpaRepository.builder<ChosenLocation>(ChosenLocation::class.java)
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
