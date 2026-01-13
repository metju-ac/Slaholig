package org.pv293.kotlinseminar.courierService.application.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class CourierNotificationService {
    private val logger = LoggerFactory.getLogger(CourierNotificationService::class.java)

    fun notifyCourierOfDeliveryOffer(
        courierId: UUID,
        offerId: UUID,
        deliveryId: UUID,
        approximateLatitude: BigDecimal,
        approximateLongitude: BigDecimal,
    ) {
        // Mock email sending - in production this would integrate with an email service
        logger.info("========================================")
        logger.info("MOCK EMAIL TO COURIER")
        logger.info("========================================")
        logger.info("To: courier-$courierId@slaholig.cz")
        logger.info("Subject: New Delivery Offer Available")
        logger.info("")
        logger.info("Dear Courier,")
        logger.info("")
        logger.info("A new delivery offer is available for you:")
        logger.info("")
        logger.info("  Offer ID:    $offerId")
        logger.info("  Delivery ID: $deliveryId")
        logger.info("  Approximate Location: ($approximateLatitude, $approximateLongitude)")
        logger.info("")
        logger.info("Please accept this offer through the courier app to receive")
        logger.info("the exact pickup location and package details.")
        logger.info("")
        logger.info("Best regards,")
        logger.info("Slaholig Automated System")
        logger.info("========================================")
    }
}
