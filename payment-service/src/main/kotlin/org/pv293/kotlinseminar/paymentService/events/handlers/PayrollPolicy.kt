package org.pv293.kotlinseminar.paymentService.events.handlers

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.paymentService.application.aggregates.PaymentStatus
import org.pv293.kotlinseminar.paymentService.application.commands.impl.ReleaseFundsCommand
import org.pv293.kotlinseminar.paymentService.application.services.PayrollService
import org.pv293.kotlinseminar.paymentService.repository.PaymentRepository
import org.pv293.kotlinseminar.productDeliveryService.events.impl.PackageRetrievedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * PayrollPolicy handles the release of funds to merchants when customers retrieve their packages.
 * This ensures merchants only get paid after successful delivery confirmation.
 */
@Component
class PayrollPolicy(
    private val commandGateway: CommandGateway,
    private val paymentRepository: PaymentRepository,
    private val payrollService: PayrollService,
) {
    private val logger = LoggerFactory.getLogger(PayrollPolicy::class.java)

    @EventHandler
    fun on(event: PackageRetrievedEvent) {
        logger.info("========================================")
        logger.info("PAYROLL POLICY TRIGGERED")
        logger.info("========================================")
        logger.info("Received PackageRetrievedEvent for order ${event.orderId}")
        logger.info("Delivery ID: ${event.deliveryId}")
        logger.info("Retrieved at: ${event.retrievedAt}")
        logger.info("========================================")

        // Idempotency check: verify payment exists and is in correct status
        val payment = paymentRepository.findById(event.orderId).orElse(null)
        
        if (payment == null) {
            logger.warn("Payment not found for order ${event.orderId}, skipping funds release")
            return
        }

        when (payment.status) {
            PaymentStatus.PAID -> {
                logger.info("Payment status is PAID, proceeding with funds release")
                
                // Call mock payroll service to simulate fund release
                val success = payrollService.mockReleaseFunds(
                    orderId = event.orderId,
                    transactionId = payment.transactionId ?: "UNKNOWN"
                )
                
                if (success) {
                    // Send command to release funds (updates aggregate state)
                    commandGateway.sendAndWait<Any>(
                        ReleaseFundsCommand(orderId = event.orderId)
                    )
                    logger.info("Funds released successfully for order ${event.orderId}")
                } else {
                    logger.error("Failed to release funds for order ${event.orderId}")
                }
            }
            PaymentStatus.RELEASED -> {
                logger.warn("Payment already released for order ${event.orderId}, skipping")
            }
            else -> {
                logger.warn("Payment status is ${payment.status} for order ${event.orderId}, expected PAID. Skipping funds release.")
            }
        }
    }
}
