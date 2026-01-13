package org.pv293.kotlinseminar.courierService.application.queries.impl

import java.util.UUID

data class AvailableDeliveryOffersQuery(
    val courierId: UUID? = null,
    val status: String? = null,
)
