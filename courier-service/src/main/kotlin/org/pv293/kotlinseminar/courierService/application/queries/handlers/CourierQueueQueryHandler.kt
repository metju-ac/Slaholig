package org.pv293.kotlinseminar.courierService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.courierService.application.dto.AvailableCourierDTO
import org.pv293.kotlinseminar.courierService.application.queries.impl.AvailableCouriersQuery
import org.pv293.kotlinseminar.courierService.repository.CourierQueueRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Component
class CourierQueueQueryHandler(
    private val courierQueueRepository: CourierQueueRepository,
) {
    private val logger = LoggerFactory.getLogger(CourierQueueQueryHandler::class.java)

    @QueryHandler
    fun handle(query: AvailableCouriersQuery): List<AvailableCourierDTO> {
        logger.info(
            "Handling AvailableCouriersQuery: nearLat=${query.nearLatitude}, nearLon=${query.nearLongitude}, radiusKm=${query.radiusKm}"
        )

        val availableCouriers = courierQueueRepository.findByAvailableTrue()

        val nearLat = query.nearLatitude
        val nearLon = query.nearLongitude
        val radius = query.radiusKm

        val filteredCouriers = if (nearLat != null && nearLon != null && radius != null) {
            availableCouriers.filter { courier ->
                val distance = calculateDistanceKm(
                    nearLat,
                    nearLon,
                    courier.latitude,
                    courier.longitude,
                )
                distance <= radius
            }
        } else {
            availableCouriers
        }

        return filteredCouriers.map { courier ->
            AvailableCourierDTO(
                courierId = courier.courierId,
                latitude = courier.latitude,
                longitude = courier.longitude,
                lastUpdatedAt = courier.lastUpdatedAt,
            )
        }
    }

    /**
     * Calculate distance between two coordinates using Haversine formula.
     * @return distance in kilometers
     */
    private fun calculateDistanceKm(
        lat1: BigDecimal,
        lon1: BigDecimal,
        lat2: BigDecimal,
        lon2: BigDecimal,
    ): Double {
        val earthRadiusKm = 6371.0

        val lat1Rad = Math.toRadians(lat1.toDouble())
        val lat2Rad = Math.toRadians(lat2.toDouble())
        val deltaLat = Math.toRadians((lat2 - lat1).toDouble())
        val deltaLon = Math.toRadians((lon2 - lon1).toDouble())

        val a = sin(deltaLat / 2).pow(2) +
            cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusKm * c
    }
}
