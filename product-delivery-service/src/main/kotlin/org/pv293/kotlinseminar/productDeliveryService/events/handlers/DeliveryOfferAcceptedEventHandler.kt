package org.pv293.kotlinseminar.productDeliveryService.events.handlers

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.courierService.events.impl.DeliveryOfferAcceptedEvent
import org.pv293.kotlinseminar.productDeliveryService.application.commands.impl.AssignCourierCommand
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Handles DeliveryOfferAcceptedEvent from courier-service.
 * When a courier accepts a delivery offer, this handler assigns the courier to the delivery.
 */
@Component
class DeliveryOfferAcceptedEventHandler(
    private val commandGateway: CommandGateway,
) {
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

        logger.info("Assigning courier ${event.courierId} to delivery ${event.deliveryId}")

        commandGateway.send<Any>(
            AssignCourierCommand(
                deliveryId = event.deliveryId,
                courierId = event.courierId,
                offerId = event.offerId,
            ),
        )
    }
}
