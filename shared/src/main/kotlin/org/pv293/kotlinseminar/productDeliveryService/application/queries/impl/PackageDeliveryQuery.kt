package org.pv293.kotlinseminar.productDeliveryService.application.queries.impl

import java.util.UUID

data class PackageDeliveryQuery(
    val deliveryId: UUID? = null,
    val orderId: UUID? = null,
)
