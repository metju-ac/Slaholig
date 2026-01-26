package org.pv293.kotlinseminar.productDeliveryService.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PackageDeliveryDTO(
    val deliveryId: UUID,
    val orderId: UUID,
    val transactionId: String,
    val status: String,
    val createdAt: Instant,
    val droppedByBakerAt: Instant? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val photoUrl: String? = null,
    val courierId: UUID? = null,
    val offerId: UUID? = null,
    val courierAssignedAt: Instant? = null,
    val pickedUpAt: Instant? = null,
    val customerLatitude: BigDecimal? = null,
    val customerLongitude: BigDecimal? = null,
    val droppedByCourierAt: Instant? = null,
    val courierDropLatitude: BigDecimal? = null,
    val courierDropLongitude: BigDecimal? = null,
    val courierDropPhotoUrl: String? = null,
)
