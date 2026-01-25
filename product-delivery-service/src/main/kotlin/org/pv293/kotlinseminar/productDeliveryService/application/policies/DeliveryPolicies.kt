package org.pv293.kotlinseminar.productDeliveryService.application.policies

import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.paymentService.events.impl.PaymentMarkedPaidEvent
import org.pv293.kotlinseminar.productDeliveryService.application.services.EmailNotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DeliveryPolicies(
    private val emailNotificationService: EmailNotificationService,
) {
    private val logger = LoggerFactory.getLogger(DeliveryPolicies::class.java)

    @EventHandler
    fun on(event: PaymentMarkedPaidEvent) {
        logger.info("Received PaymentMarkedPaidEvent for order ${event.orderId}, transaction ${event.transactionId}")
        
        // Send mock email notification to baker
        emailNotificationService.notifyBaker(
            orderId = event.orderId,
            transactionId = event.transactionId,
        )
        
        logger.info("Baker notification sent for order ${event.orderId}")
    }
}
