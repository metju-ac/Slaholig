package org.pv293.kotlinseminar.productDeliveryService.application.aggregates

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.spring.stereotype.Aggregate
import org.pv293.kotlinseminar.productDeliveryService.application.commands.impl.CreatePackageDeliveryCommand
import org.pv293.kotlinseminar.productDeliveryService.events.impl.PackageDeliveryCreatedEvent
import java.time.Instant
import java.util.UUID

enum class DeliveryStatus {
    CREATED,
    DROPPED_BY_BAKER,
    IN_TRANSIT,
    DROPPED_BY_COURIER,
    DELIVERED,
}

@Entity
@Aggregate(repository = "packageDeliveryAggregateRepository")
@Table(name = "package_delivery")
class PackageDelivery() {
    @Id
    @AggregateIdentifier
    lateinit var deliveryId: UUID

    lateinit var orderId: UUID

    lateinit var transactionId: String

    @Enumerated(EnumType.STRING)
    lateinit var status: DeliveryStatus

    @Column(name = "created_at")
    lateinit var createdAt: Instant

    @CommandHandler
    constructor(command: CreatePackageDeliveryCommand) : this() {
        apply(
            PackageDeliveryCreatedEvent(
                deliveryId = command.deliveryId,
                orderId = command.orderId,
                transactionId = command.transactionId,
                status = DeliveryStatus.CREATED.name,
                createdAt = Instant.now(),
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: PackageDeliveryCreatedEvent) {
        this.deliveryId = event.deliveryId
        this.orderId = event.orderId
        this.transactionId = event.transactionId
        this.status = DeliveryStatus.valueOf(event.status)
        this.createdAt = event.createdAt
    }
}
