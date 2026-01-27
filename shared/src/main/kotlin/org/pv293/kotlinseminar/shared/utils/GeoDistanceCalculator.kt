package org.pv293.kotlinseminar.shared.utils

import java.math.BigDecimal
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utility for calculating geographic distances between coordinates.
 */
object GeoDistanceCalculator {
    /**
     * Calculate distance between two coordinates using Haversine formula.
     * @param lat1 First latitude in degrees
     * @param lon1 First longitude in degrees
     * @param lat2 Second latitude in degrees
     * @param lon2 Second longitude in degrees
     * @return distance in meters
     */
    fun calculateDistanceMeters(
        lat1: BigDecimal,
        lon1: BigDecimal,
        lat2: BigDecimal,
        lon2: BigDecimal,
    ): Double {
        val earthRadiusM = 6371000.0

        val lat1Rad = Math.toRadians(lat1.toDouble())
        val lat2Rad = Math.toRadians(lat2.toDouble())
        val deltaLat = Math.toRadians((lat2 - lat1).toDouble())
        val deltaLon = Math.toRadians((lon2 - lon1).toDouble())

        val a = sin(deltaLat / 2).pow(2) +
            cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusM * c
    }
}
