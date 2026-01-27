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

    fun notifyCustomerDelivered(orderId: UUID, deliveryId: UUID) {
        // Mock email sending - in production this would integrate with an email service
        logger.info("========================================")
        logger.info("📧 MOCK EMAIL TO CUSTOMER")
        logger.info("========================================")
        logger.info("To: customer@example.com")
        logger.info("Subject: Your Package Has Been Delivered!")
        logger.info("")
        logger.info("Dear Customer,")
        logger.info("")
        logger.info("Great news! Your order has been successfully delivered:")
        logger.info("")
        logger.info("  Order ID:    $orderId")
        logger.info("  Delivery ID: $deliveryId")
        logger.info("")
        logger.info("Your package has been delivered to your specified location.")
        logger.info("You can now retrieve your delicious baked goods!")
        logger.info("")
        logger.info("Thank you for choosing Slaholig!")
        logger.info("")
        logger.info("Best regards,")
        logger.info("Slaholig Team")
        logger.info("========================================")
    }
}
