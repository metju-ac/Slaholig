package org.pv293.kotlinseminar.courierService.application.aggregates

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.spring.stereotype.Aggregate
import org.pv293.kotlinseminar.courierService.application.commands.impl.MarkCourierAvailableCommand
import org.pv293.kotlinseminar.courierService.application.commands.impl.MarkCourierUnavailableCommand
import org.pv293.kotlinseminar.courierService.application.commands.impl.UpdateCourierLocationCommand
import org.pv293.kotlinseminar.courierService.events.impl.CourierLocationUpdatedEvent
import org.pv293.kotlinseminar.courierService.events.impl.CourierMarkedAvailableEvent
import org.pv293.kotlinseminar.courierService.events.impl.CourierMarkedUnavailableEvent
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Aggregate(repository = "courierQueueAggregateRepository")
@Table(name = "courier_queue")
class CourierQueue() {
    @Id
    @AggregateIdentifier
    lateinit var courierId: UUID

    @Column(nullable = false, precision = 10, scale = 7)
    lateinit var latitude: BigDecimal

    @Column(nullable = false, precision = 10, scale = 7)
    lateinit var longitude: BigDecimal

    @Column(nullable = false)
    var available: Boolean = false

    @Column(name = "last_updated_at", nullable = false)
    lateinit var lastUpdatedAt: Instant

    @CommandHandler
    constructor(command: MarkCourierAvailableCommand) : this() {
        apply(
            CourierMarkedAvailableEvent(
                courierId = command.courierId,
                latitude = command.latitude,
                longitude = command.longitude,
                availableAt = Instant.now(),
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: CourierMarkedAvailableEvent) {
        this.courierId = event.courierId
        this.latitude = event.latitude
        this.longitude = event.longitude
        this.available = true
        this.lastUpdatedAt = event.availableAt
    }

    @CommandHandler
    fun handle(command: MarkCourierUnavailableCommand) {
        if (!available) {
            return // Already unavailable, nothing to do
        }

        apply(
            CourierMarkedUnavailableEvent(
                courierId = command.courierId,
                unavailableAt = Instant.now(),
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: CourierMarkedUnavailableEvent) {
        this.available = false
        this.lastUpdatedAt = event.unavailableAt
    }

    @CommandHandler
    fun handle(command: UpdateCourierLocationCommand) {
        require(available) {
            "Courier must be available to update location. Mark as available first."
        }

        apply(
            CourierLocationUpdatedEvent(
                courierId = command.courierId,
                latitude = command.latitude,
                longitude = command.longitude,
                updatedAt = Instant.now(),
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: CourierLocationUpdatedEvent) {
        this.latitude = event.latitude
        this.longitude = event.longitude
        this.lastUpdatedAt = event.updatedAt
    }
}
