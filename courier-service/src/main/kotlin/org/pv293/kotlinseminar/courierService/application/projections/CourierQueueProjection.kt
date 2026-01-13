package org.pv293.kotlinseminar.courierService.application.projections

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Read model projection for courier availability and location.
 * This is NOT an Axon aggregate, just a JPA entity for querying.
 */
@Entity
@Table(name = "courier_queue_projection")
data class CourierQueueProjection(
    @Id
    val courierId: UUID,

    @Column(nullable = false, precision = 10, scale = 7)
    var latitude: BigDecimal,

    @Column(nullable = false, precision = 10, scale = 7)
    var longitude: BigDecimal,

    @Column(nullable = false)
    var available: Boolean = false,

    @Column(name = "last_updated_at", nullable = false)
    var lastUpdatedAt: Instant,
)
