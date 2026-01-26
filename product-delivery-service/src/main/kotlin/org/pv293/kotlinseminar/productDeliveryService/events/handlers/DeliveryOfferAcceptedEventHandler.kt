package org.pv293.kotlinseminar.productDeliveryService.events.handlers

import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.courierService.events.impl.DeliveryOfferAcceptedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handles DeliveryOfferAcceptedEvent from courier-service.
 * When a courier accepts a delivery offer, this handler logs the acceptance
 * and could update the PackageDelivery aggregate to track the assigned courier.
 */
@Component
class DeliveryOfferAcceptedEventHandler {
    private val logger = LoggerFactory.getLogger(DeliveryOfferAcceptedEventHandler::class.java)

    @EventHandler
    fun on(event: DeliveryOfferAcceptedEvent) {
        logger.info("========================================")
        logger.info("DELIVERY OFFER ACCEPTED")
        logger.info("========================================")
        logger.info("Courier ${event.courierId} accepted delivery ${event.deliveryId}")
        logger.info("Offer ID: ${event.offerId}")
        logger.info("Order ID: ${event.orderId}")
        logger.info("Accepted at: ${event.acceptedAt}")
        logger.info("========================================")

        // In a full implementation, this could:
        // 1. Send a command to update the PackageDelivery aggregate with assigned courier
        // 2. Cancel other pending offers for the same delivery
        // 3. Notify the baker that a courier has been assigned
    }
}
