package org.pv293.kotlinseminar.courierService.application.policies

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.courierService.application.aggregates.OfferStatus
import org.pv293.kotlinseminar.courierService.application.commands.impl.CancelDeliveryOfferCommand
import org.pv293.kotlinseminar.courierService.events.impl.DeliveryOfferAcceptedEvent
import org.pv293.kotlinseminar.courierService.repository.AvailableDeliveryOfferRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Policy that reacts to DeliveryOfferAcceptedEvent by cancelling all other pending offers
 * for the same delivery.
 */
@Component
class OfferAcceptedPolicy(
    private val deliveryOfferRepository: AvailableDeliveryOfferRepository,
    private val commandGateway: CommandGateway,
) {
    private val logger = LoggerFactory.getLogger(OfferAcceptedPolicy::class.java)

    @EventHandler
    fun on(event: DeliveryOfferAcceptedEvent) {
        logger.info(
            "Offer ${event.offerId} accepted, cancelling other pending offers for delivery ${event.deliveryId}"
        )

        // Find all PENDING offers for this delivery
        val pendingOffers = deliveryOfferRepository.findByDeliveryIdAndStatus(
            event.deliveryId,
            OfferStatus.PENDING,
        )

        logger.info("Found ${pendingOffers.size} pending offer(s) to cancel")

        pendingOffers.forEach { offer ->
            commandGateway.send<Any>(
                CancelDeliveryOfferCommand(
                    offerId = offer.offerId,
                    reason = "Another courier accepted delivery ${event.deliveryId}",
                ),
            )
            logger.info("Cancelled offer ${offer.offerId} for courier ${offer.courierId}")
        }
    }
}
