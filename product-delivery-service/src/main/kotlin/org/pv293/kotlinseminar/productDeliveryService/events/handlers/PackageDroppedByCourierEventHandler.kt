package org.pv293.kotlinseminar.productDeliveryService.events.handlers

import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.productDeliveryService.application.services.EmailNotificationService
import org.pv293.kotlinseminar.productDeliveryService.events.impl.PackageDroppedByCourierEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PackageDroppedByCourierEventHandler(
    private val emailNotificationService: EmailNotificationService,
) {
    private val logger = LoggerFactory.getLogger(PackageDroppedByCourierEventHandler::class.java)

    @EventHandler
    fun on(event: PackageDroppedByCourierEvent) {
        logger.info("Package ${event.deliveryId} delivered by courier ${event.courierId} at (${event.latitude}, ${event.longitude})")

        emailNotificationService.notifyCustomerDelivered(
            orderId = event.orderId,
            deliveryId = event.deliveryId,
        )

        logger.info("Customer notification sent for delivery ${event.deliveryId}")
    }
}
