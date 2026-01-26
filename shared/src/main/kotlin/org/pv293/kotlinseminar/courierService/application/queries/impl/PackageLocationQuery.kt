package org.pv293.kotlinseminar.courierService.application.queries.impl

import java.math.BigDecimal
import java.util.UUID

data class PackageLocationQuery(
    val offerId: UUID,
    val courierId: UUID,
    val courierLatitude: BigDecimal,
    val courierLongitude: BigDecimal,
)
