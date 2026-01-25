package org.pv293.kotlinseminar.courierService.infrastructure

import com.thoughtworks.xstream.XStream
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.axonframework.common.jpa.EntityManagerProvider
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AxonConfig {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Bean
    fun entityManagerProvider(): EntityManagerProvider {
        return EntityManagerProvider { entityManager }
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
