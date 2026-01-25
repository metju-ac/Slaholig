package org.pv293.kotlinseminar.courierService.application.aggregates

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
import org.pv293.kotlinseminar.courierService.application.commands.impl.AcceptDeliveryOfferCommand
import org.pv293.kotlinseminar.courierService.application.commands.impl.CreateDeliveryOfferCommand
import org.pv293.kotlinseminar.courierService.events.impl.DeliveryOfferAcceptedEvent
import org.pv293.kotlinseminar.courierService.events.impl.DeliveryOfferCreatedEvent
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Aggregate(repository = "deliveryOfferAggregateRepository")
@Table(name = "available_delivery_offer")
class AvailableDeliveryOffer() {

    @Id
    @AggregateIdentifier
    lateinit var offerId: UUID

    @Column(nullable = false)
    lateinit var deliveryId: UUID

    @Column(nullable = false)
    lateinit var orderId: UUID

    @Column(nullable = false)
    lateinit var courierId: UUID

    @Column(nullable = false, precision = 10, scale = 2)
    lateinit var approximateLatitude: BigDecimal

    @Column(nullable = false, precision = 10, scale = 2)
    lateinit var approximateLongitude: BigDecimal

    @Column(name = "dropped_at", nullable = false)
    lateinit var droppedAt: Instant

    @Column(name = "offered_at", nullable = false)
    lateinit var offeredAt: Instant

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OfferStatus = OfferStatus.PENDING

    @CommandHandler
    constructor(command: CreateDeliveryOfferCommand) : this() {
        apply(
            DeliveryOfferCreatedEvent(
                offerId = command.offerId,
                deliveryId = command.deliveryId,
                orderId = command.orderId,
                courierId = command.courierId,
                approximateLatitude = command.approximateLatitude,
                approximateLongitude = command.approximateLongitude,
                droppedAt = command.droppedAt,
                offeredAt = Instant.now(),
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: DeliveryOfferCreatedEvent) {
        this.offerId = event.offerId
        this.deliveryId = event.deliveryId
        this.orderId = event.orderId
        this.courierId = event.courierId
        this.approximateLatitude = event.approximateLatitude
        this.approximateLongitude = event.approximateLongitude
        this.droppedAt = event.droppedAt
        this.offeredAt = event.offeredAt
        this.status = OfferStatus.PENDING
    }

    @CommandHandler
    fun handle(command: AcceptDeliveryOfferCommand) {
        require(command.courierId == this.courierId) {
            "Offer $offerId is not for courier ${command.courierId}"
        }
        require(status == OfferStatus.PENDING) {
            "Offer $offerId is already ${status.name}"
        }

        apply(
            DeliveryOfferAcceptedEvent(
                offerId = offerId,
                deliveryId = deliveryId,
                orderId = orderId,
                courierId = courierId,
                acceptedAt = Instant.now(),
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: DeliveryOfferAcceptedEvent) {
        this.status = OfferStatus.ACCEPTED
    }
}
