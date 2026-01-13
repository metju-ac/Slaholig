package org.pv293.kotlinseminar.courierService.application.dto

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class AvailableDeliveryOfferDTO(
    val offerId: UUID,
    val deliveryId: UUID,
    val orderId: UUID,
    val courierId: UUID,
    val approximateLatitude: BigDecimal,
    val approximateLongitude: BigDecimal,
    val droppedAt: Instant,
    val offeredAt: Instant,
    val status: String,
)
