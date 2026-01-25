package org.pv293.kotlinseminar.productDeliveryService.application.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EmailNotificationService {
    private val logger = LoggerFactory.getLogger(EmailNotificationService::class.java)
    
    fun notifyBaker(orderId: UUID, transactionId: String) {
        // Mock email sending - in production this would integrate with an email service
        logger.info("========================================")
        logger.info("📧 MOCK EMAIL TO BAKER")
        logger.info("========================================")
        logger.info("To: baker@slaholig.cz")
        logger.info("Subject: New Order Ready for Delivery")
        logger.info("")
        logger.info("Dear Baker,")
        logger.info("")
        logger.info("A new order has been successfully paid and is ready for preparation:")
        logger.info("")
        logger.info("  Order ID:       $orderId")
        logger.info("  Transaction ID: $transactionId")
        logger.info("")
        logger.info("Please prepare the baked goods and arrange for delivery.")
        logger.info("")
        logger.info("Best regards,")
        logger.info("Slaholig Automated System")
        logger.info("========================================")
    }
}
