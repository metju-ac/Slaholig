package org.pv293.kotlinseminar.productDeliveryService.events.handlers

import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.productDeliveryService.application.aggregates.DeliveryLocation
import org.pv293.kotlinseminar.productDeliveryService.events.impl.PackageDroppedByBakerEvent
import org.pv293.kotlinseminar.productDeliveryService.repository.DeliveryLocationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PackageDroppedByBakerEventHandler(
    private val deliveryLocationRepository: DeliveryLocationRepository,
) {
    private val logger = LoggerFactory.getLogger(PackageDroppedByBakerEventHandler::class.java)

    @EventHandler
    fun on(event: PackageDroppedByBakerEvent) {
        logger.info("Received PackageDroppedByBakerEvent for delivery ${event.deliveryId}")

        val location = DeliveryLocation(
            deliveryId = event.deliveryId,
            orderId = event.orderId,
            latitude = event.latitude,
            longitude = event.longitude,
            photoUrl = event.photoUrl,
            droppedAt = event.droppedAt,
        )

        deliveryLocationRepository.save(location)

        logger.info("DeliveryLocation read model created for delivery ${event.deliveryId} at (${event.latitude}, ${event.longitude})")
    }
}
