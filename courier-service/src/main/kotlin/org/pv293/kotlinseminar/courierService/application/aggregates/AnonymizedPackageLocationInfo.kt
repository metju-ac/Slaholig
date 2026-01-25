package org.pv293.kotlinseminar.courierService.application.aggregates

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Anonymized location info for couriers to view available deliveries.
 * Coordinates are reduced to ~100m precision (2 decimal places) and photo URL is NOT included.
 */
@Entity
@Table(name = "anonymized_package_location_info")
data class AnonymizedPackageLocationInfo(
    @Id
    val deliveryId: UUID,

    @Column(nullable = false)
    val orderId: UUID,

    @Column(nullable = false, precision = 10, scale = 2)
    val approximateLatitude: BigDecimal,

    @Column(nullable = false, precision = 10, scale = 2)
    val approximateLongitude: BigDecimal,

    @Column(nullable = false)
    val droppedAt: Instant,

    @Column(nullable = false)
    val available: Boolean = true,
)
