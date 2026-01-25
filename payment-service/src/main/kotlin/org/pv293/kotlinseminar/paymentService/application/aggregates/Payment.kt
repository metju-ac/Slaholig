package org.pv293.kotlinseminar.paymentService.application.aggregates

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.spring.stereotype.Aggregate
import org.pv293.kotlinseminar.paymentService.application.commands.impl.CreatePaymentCommand
import org.pv293.kotlinseminar.paymentService.application.commands.impl.MarkPaymentPaidCommand
import org.pv293.kotlinseminar.paymentService.application.commands.impl.PayOrderCommand
import org.pv293.kotlinseminar.paymentService.application.commands.impl.ReleaseFundsCommand
import org.pv293.kotlinseminar.paymentService.application.services.CryptoPaymentGatewayService
import org.pv293.kotlinseminar.paymentService.events.impl.FundsReleasedEvent
import org.pv293.kotlinseminar.paymentService.events.impl.PaymentCreatedEvent
import org.pv293.kotlinseminar.paymentService.events.impl.PaymentFailedEvent
import org.pv293.kotlinseminar.paymentService.events.impl.PaymentMarkedPaidEvent
import org.pv293.kotlinseminar.paymentService.events.impl.PaymentProcessingEvent
import org.pv293.kotlinseminar.paymentService.events.impl.PaymentSucceededEvent
import java.util.UUID

enum class PaymentStatus {
    CREATED,
    PROCESSING,
    PAID,
    RELEASED,
    FAILED,
}

@Entity
@Aggregate(repository = "paymentAggregateRepository")
@Table(name = "payment")
class Payment() {
    @Id
    @AggregateIdentifier
    lateinit var orderId: UUID

    lateinit var cartId: UUID

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "payment_items", joinColumns = [JoinColumn(name = "order_id")])
    var items: MutableList<PaymentItem> = mutableListOf()

    @Enumerated(EnumType.STRING)
    lateinit var status: PaymentStatus

    @Column(nullable = true)
    var transactionId: String? = null

    @Column(nullable = true)
    var failureReason: String? = null

    @CommandHandler
    constructor(command: CreatePaymentCommand) : this() {
        apply(
            PaymentCreatedEvent(
                orderId = command.orderId,
                cartId = command.cartId,
                items = command.items,
                status = PaymentStatus.CREATED,
            ),
        )
    }

    @CommandHandler
    fun handle(
        command: PayOrderCommand,
        cryptoPaymentGatewayService: CryptoPaymentGatewayService,
    ) {
        require(status == PaymentStatus.CREATED) { "Payment must be in CREATED status to pay. Current status: $status" }

        apply(PaymentProcessingEvent(orderId = command.orderId))

        // Call the crypto payment gateway
        val result = cryptoPaymentGatewayService.mockProcessPayment(
            orderId = command.orderId,
            itemCount = items.sumOf { it.quantity },
        )

        if (result.success) {
            apply(
                PaymentSucceededEvent(
                    orderId = command.orderId,
                    transactionId = result.transactionId!!,
                ),
            )
        } else {
            apply(
                PaymentFailedEvent(
                    orderId = command.orderId,
                    reason = result.errorMessage ?: "Unknown error",
                ),
            )
        }
    }

    @CommandHandler
    fun handle(command: MarkPaymentPaidCommand) {
        require(status == PaymentStatus.PROCESSING) {
            "Payment must be in PROCESSING status to mark as paid. Current status: $status"
        }

        apply(
            PaymentMarkedPaidEvent(
                orderId = command.orderId,
                transactionId = command.transactionId,
            ),
        )
    }

    @CommandHandler
    fun handle(command: ReleaseFundsCommand) {
        require(status == PaymentStatus.PAID) {
            "Payment must be in PAID status to release funds. Current status: $status"
        }

        apply(FundsReleasedEvent(orderId = command.orderId))
    }

    @EventSourcingHandler
    fun on(event: PaymentCreatedEvent) {
        this.orderId = event.orderId
        this.cartId = event.cartId
        this.items = event.items.map {
            PaymentItem(
                bakedGoodsId = it.bakedGoodsId,
                quantity = it.quantity,
            )
        }.toMutableList()
        this.status = event.status
    }

    @EventSourcingHandler
    fun on(event: PaymentProcessingEvent) {
        this.status = PaymentStatus.PROCESSING
    }

    @EventSourcingHandler
    fun on(event: PaymentSucceededEvent) {
        // Status stays PROCESSING until policy sends MarkPaymentPaidCommand
        this.transactionId = event.transactionId
    }

    @EventSourcingHandler
    fun on(event: PaymentFailedEvent) {
        this.status = PaymentStatus.FAILED
        this.failureReason = event.reason
    }

    @EventSourcingHandler
    fun on(event: PaymentMarkedPaidEvent) {
        this.status = PaymentStatus.PAID
        this.transactionId = event.transactionId
    }

    @EventSourcingHandler
    fun on(event: FundsReleasedEvent) {
        this.status = PaymentStatus.RELEASED
    }
}

@jakarta.persistence.Embeddable
data class PaymentItem(
    var bakedGoodsId: UUID = UUID.randomUUID(),
    var quantity: Int = 0,
)
