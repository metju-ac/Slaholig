package org.pv293.kotlinseminar.productSelectionService.application.geo

import org.pv293.kotlinseminar.shared.utils.GeoDistanceCalculator
import java.math.BigDecimal

object GeoDistance {
    fun distanceKm(
        lat1: BigDecimal,
        lon1: BigDecimal,
        lat2: BigDecimal,
        lon2: BigDecimal,
    ): Double {
        return GeoDistanceCalculator.calculateDistanceMeters(lat1, lon1, lat2, lon2) / 1000.0
    }
}
