package org.pv293.kotlinseminar.productDeliveryService.events.handlers

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.paymentService.events.impl.PaymentMarkedPaidEvent
import org.pv293.kotlinseminar.productDeliveryService.application.commands.impl.CreatePackageDeliveryCommand
import org.pv293.kotlinseminar.productDeliveryService.application.services.EmailNotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PaymentMarkedPaidEventHandler(
    private val emailNotificationService: EmailNotificationService,
    private val commandGateway: CommandGateway,
) {
    private val logger = LoggerFactory.getLogger(PaymentMarkedPaidEventHandler::class.java)

    @EventHandler
    fun on(event: PaymentMarkedPaidEvent) {
        logger.info("Received PaymentMarkedPaidEvent for order ${event.orderId}, transaction ${event.transactionId}")
        
        // Send mock email notification to baker
        emailNotificationService.notifyBaker(
            orderId = event.orderId,
            transactionId = event.transactionId,
        )
        
        logger.info("Baker notification sent for order ${event.orderId}")
        
        // Create PackageDelivery aggregate after baker is notified
        val deliveryId = UUID.randomUUID()
        commandGateway.sendAndWait<Any>(
            CreatePackageDeliveryCommand(
                deliveryId = deliveryId,
                orderId = event.orderId,
                transactionId = event.transactionId,
            ),
        )
        
        logger.info("PackageDelivery created with ID: $deliveryId for order ${event.orderId}")
    }
}
