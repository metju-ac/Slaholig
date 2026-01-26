package org.pv293.kotlinseminar.courierService.application.aggregates

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Full location info including exact coordinates and photo URL.
 * Only accessible to couriers who have accepted the delivery and are within proximity.
 */
@Entity
@Table(name = "package_location_info")
data class PackageLocationInfo(
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
