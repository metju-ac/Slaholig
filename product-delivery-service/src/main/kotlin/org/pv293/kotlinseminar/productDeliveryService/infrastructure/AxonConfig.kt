package org.pv293.kotlinseminar.productDeliveryService.infrastructure

import com.thoughtworks.xstream.XStream
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.xml.XStreamSerializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AxonConfig {

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
