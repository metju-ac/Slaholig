package org.pv293.kotlinseminar.courierService.application.queries.impl

import java.math.BigDecimal

data class AvailableCouriersQuery(
    val nearLatitude: BigDecimal? = null,
    val nearLongitude: BigDecimal? = null,
    val radiusKm: Double? = null,
)
