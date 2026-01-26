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
import org.pv293.kotlinseminar.productDeliveryService.application.commands.impl.AssignCourierCommand
import org.pv293.kotlinseminar.productDeliveryService.application.commands.impl.CreatePackageDeliveryCommand
import org.pv293.kotlinseminar.productDeliveryService.application.commands.impl.MarkDroppedByBakerCommand
import org.pv293.kotlinseminar.productDeliveryService.application.commands.impl.MarkDroppedByCourierCommand
import org.pv293.kotlinseminar.productDeliveryService.application.commands.impl.MarkPickedUpByCourierCommand
import org.pv293.kotlinseminar.productDeliveryService.events.impl.CourierAssignedEvent
import org.pv293.kotlinseminar.productDeliveryService.events.impl.PackageDeliveryCreatedEvent
import org.pv293.kotlinseminar.productDeliveryService.events.impl.PackageDroppedByBakerEvent
import org.pv293.kotlinseminar.productDeliveryService.events.impl.PackageDroppedByCourierEvent
import org.pv293.kotlinseminar.productDeliveryService.events.impl.PackagePickedUpByCourierEvent
import org.pv293.kotlinseminar.shared.utils.GeoDistanceCalculator
import java.math.BigDecimal
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

    @Column(name = "dropped_by_baker_at", nullable = true)
    var droppedByBakerAt: Instant? = null

    @Column(nullable = true, precision = 10, scale = 7)
    var latitude: BigDecimal? = null

    @Column(nullable = true, precision = 10, scale = 7)
    var longitude: BigDecimal? = null

    @Column(nullable = true, length = 500)
    var photoUrl: String? = null

    @Column(nullable = true)
    var courierId: UUID? = null

    @Column(nullable = true)
    var offerId: UUID? = null

    @Column(name = "courier_assigned_at", nullable = true)
    var courierAssignedAt: Instant? = null

    @Column(name = "picked_up_at", nullable = true)
    var pickedUpAt: Instant? = null

    @Column(name = "customer_latitude", nullable = true, precision = 10, scale = 7)
    var customerLatitude: BigDecimal? = null

    @Column(name = "customer_longitude", nullable = true, precision = 10, scale = 7)
    var customerLongitude: BigDecimal? = null

    @Column(name = "dropped_by_courier_at", nullable = true)
    var droppedByCourierAt: Instant? = null

    @Column(name = "courier_drop_latitude", nullable = true, precision = 10, scale = 7)
    var courierDropLatitude: BigDecimal? = null

    @Column(name = "courier_drop_longitude", nullable = true, precision = 10, scale = 7)
    var courierDropLongitude: BigDecimal? = null

    @Column(name = "courier_drop_photo_url", nullable = true, length = 500)
    var courierDropPhotoUrl: String? = null

    @CommandHandler
    constructor(command: CreatePackageDeliveryCommand) : this() {
        apply(
            PackageDeliveryCreatedEvent(
                deliveryId = command.deliveryId,
                orderId = command.orderId,
                transactionId = command.transactionId,
                status = DeliveryStatus.CREATED.name,
                createdAt = Instant.now(),
                customerLatitude = command.customerLatitude,
                customerLongitude = command.customerLongitude,
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
        this.customerLatitude = event.customerLatitude
        this.customerLongitude = event.customerLongitude
    }

    @CommandHandler
    fun handle(command: MarkDroppedByBakerCommand) {
        require(status == DeliveryStatus.CREATED) {
            "Package must be in CREATED status to be dropped by baker. Current status: $status"
        }

        apply(
            PackageDroppedByBakerEvent(
                deliveryId = command.deliveryId,
                orderId = this.orderId,
                droppedAt = Instant.now(),
                latitude = command.latitude,
                longitude = command.longitude,
                photoUrl = command.photoUrl,
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: PackageDroppedByBakerEvent) {
        this.status = DeliveryStatus.DROPPED_BY_BAKER
        this.droppedByBakerAt = event.droppedAt
        this.latitude = event.latitude
        this.longitude = event.longitude
        this.photoUrl = event.photoUrl
    }

    @CommandHandler
    fun handle(command: AssignCourierCommand) {
        require(status == DeliveryStatus.DROPPED_BY_BAKER) {
            "Package must be DROPPED_BY_BAKER to assign courier. Current status: $status"
        }
        require(courierId == null) {
            "Courier already assigned: $courierId"
        }

        apply(
            CourierAssignedEvent(
                deliveryId = deliveryId,
                courierId = command.courierId,
                offerId = command.offerId,
                assignedAt = Instant.now(),
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: CourierAssignedEvent) {
        this.courierId = event.courierId
        this.offerId = event.offerId
        this.courierAssignedAt = event.assignedAt
    }

    @CommandHandler
    fun handle(command: MarkPickedUpByCourierCommand) {
        require(status == DeliveryStatus.DROPPED_BY_BAKER) {
            "Package must be DROPPED_BY_BAKER to be picked up. Current status: $status"
        }
        require(courierId == command.courierId) {
            "Package is assigned to courier $courierId, not ${command.courierId}"
        }

        apply(
            PackagePickedUpByCourierEvent(
                deliveryId = deliveryId,
                courierId = command.courierId,
                pickedUpAt = Instant.now(),
            ),
        )
    }

    @EventSourcingHandler
    fun on(event: PackagePickedUpByCourierEvent) {
        this.status = DeliveryStatus.IN_TRANSIT
        this.pickedUpAt = event.pickedUpAt
    }

    @CommandHandler
    fun handle(command: MarkDroppedByCourierCommand) {
        require(status == DeliveryStatus.IN_TRANSIT) {
            "Package must be IN_TRANSIT to be dropped by courier. Current status: $status"
        }
        require(courierId == command.courierId) {
            "Package is assigned to courier $courierId, not ${command.courierId}"
        }
        require(customerLatitude != null && customerLongitude != null) {
            "Customer delivery address not set for this delivery"
        }

        // Validate drop location is within 100m of customer address
        val distanceMeters = GeoDistanceCalculator.calculateDistanceMeters(
            command.latitude,
            command.longitude,
            customerLatitude!!,
            customerLongitude!!
        )

        require(distanceMeters <= 100.0) {
            "Drop location is ${distanceMeters.toInt()}m from customer address (max 100m allowed)"
        }

        apply(
            PackageDroppedByCourierEvent(
                deliveryId = deliveryId,
                orderId = orderId,
                courierId = command.courierId,
                droppedAt = Instant.now(),
                latitude = command.latitude,
                longitude = command.longitude,
                photoUrl = command.photoUrl,
            )
        )
    }

    @EventSourcingHandler
    fun on(event: PackageDroppedByCourierEvent) {
        this.status = DeliveryStatus.DROPPED_BY_COURIER
        this.droppedByCourierAt = event.droppedAt
        this.courierDropLatitude = event.latitude
        this.courierDropLongitude = event.longitude
        this.courierDropPhotoUrl = event.photoUrl
    }
}
