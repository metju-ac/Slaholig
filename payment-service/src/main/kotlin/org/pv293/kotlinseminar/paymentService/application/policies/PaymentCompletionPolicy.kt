package org.pv293.kotlinseminar.paymentService.application.policies

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.paymentService.application.commands.impl.MarkPaymentPaidCommand
import org.pv293.kotlinseminar.paymentService.events.impl.PaymentSucceededEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("payment-service")
class PaymentCompletionPolicy(
    private val commandGateway: CommandGateway,
) {
    private val logger = LoggerFactory.getLogger(PaymentCompletionPolicy::class.java)

    @EventHandler
    fun on(event: PaymentSucceededEvent) {
        logger.info("Payment succeeded for order ${event.orderId}, marking as PAID")

        commandGateway.sendAndWait<Any>(
            MarkPaymentPaidCommand(
                orderId = event.orderId,
                transactionId = event.transactionId,
            ),
        )

        logger.info("Payment marked as PAID for order ${event.orderId}")
    }
}
