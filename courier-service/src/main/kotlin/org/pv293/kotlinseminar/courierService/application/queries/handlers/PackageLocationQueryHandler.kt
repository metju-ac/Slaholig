package org.pv293.kotlinseminar.courierService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.courierService.application.aggregates.OfferStatus
import org.pv293.kotlinseminar.courierService.application.dto.PackageLocationDTO
import org.pv293.kotlinseminar.courierService.application.queries.impl.PackageLocationQuery
import org.pv293.kotlinseminar.courierService.repository.AnonymizedPackageLocationInfoRepository
import org.pv293.kotlinseminar.courierService.repository.AvailableDeliveryOfferRepository
import org.pv293.kotlinseminar.courierService.repository.PackageLocationInfoRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Component
class PackageLocationQueryHandler(
    private val deliveryOfferRepository: AvailableDeliveryOfferRepository,
    private val packageLocationRepository: PackageLocationInfoRepository,
    private val anonymizedLocationRepository: AnonymizedPackageLocationInfoRepository,
) {
    private val logger = LoggerFactory.getLogger(PackageLocationQueryHandler::class.java)

    companion object {
        private const val PROXIMITY_THRESHOLD_METERS = 500.0
    }

    @QueryHandler
    fun handle(query: PackageLocationQuery): PackageLocationDTO? {
        // 1. Verify offer is ACCEPTED by this courier
        val offer = deliveryOfferRepository.findById(query.offerId).orElse(null)
            ?: throw IllegalArgumentException("Offer ${query.offerId} not found")

        if (offer.courierId != query.courierId) {
            throw IllegalArgumentException("Offer ${query.offerId} is not for courier ${query.courierId}")
        }

        if (offer.status != OfferStatus.ACCEPTED) {
            throw IllegalArgumentException("Offer ${query.offerId} is not accepted (status: ${offer.status})")
        }

        // 2. Get package location
        val fullLocation = packageLocationRepository.findById(offer.deliveryId).orElse(null)
            ?: return null

        // 3. Calculate distance
        val distanceMeters = calculateDistanceMeters(
            query.courierLatitude,
            query.courierLongitude,
            fullLocation.latitude,
            fullLocation.longitude,
        )

        logger.info("Courier ${query.courierId} is ${distanceMeters}m from package ${offer.deliveryId}")

        // 4. Return exact or approximate based on proximity
        return if (distanceMeters <= PROXIMITY_THRESHOLD_METERS) {
            // Close enough - return full location
            PackageLocationDTO(
                deliveryId = fullLocation.deliveryId,
                orderId = fullLocation.orderId,
                latitude = fullLocation.latitude,
                longitude = fullLocation.longitude,
                photoUrl = fullLocation.photoUrl,
                isExactLocation = true,
                droppedAt = fullLocation.droppedAt,
            )
        } else {
            // Too far - return approximate
            val approxLocation = anonymizedLocationRepository.findById(offer.deliveryId).orElse(null)
                ?: return null

            PackageLocationDTO(
                deliveryId = approxLocation.deliveryId,
                orderId = approxLocation.orderId,
                latitude = approxLocation.approximateLatitude,
                longitude = approxLocation.approximateLongitude,
                photoUrl = null,
                isExactLocation = false,
                droppedAt = approxLocation.droppedAt,
            )
        }
    }

    /**
     * Calculate distance between two coordinates using Haversine formula.
     * @return distance in meters
     */
    private fun calculateDistanceMeters(
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
