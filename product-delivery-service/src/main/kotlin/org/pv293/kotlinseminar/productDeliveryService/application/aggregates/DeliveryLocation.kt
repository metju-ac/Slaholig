package org.pv293.kotlinseminar.productDeliveryService.application.aggregates

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "delivery_location")
data class DeliveryLocation(
    @Id
    val deliveryId: UUID,

    @Column(nullable = false)
    val orderId: UUID,

    @Column(nullable = false, precision = 10, scale = 7)
    val latitude: BigDecimal,

    @Column(nullable = false, precision = 10, scale = 7)
    val longitude: BigDecimal,

    @Column(nullable = false, length = 500)
    val photoUrl: String,

    @Column(nullable = false)
    val droppedAt: Instant,
)
