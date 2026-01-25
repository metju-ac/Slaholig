package org.pv293.kotlinseminar.courierService.application.policies

import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.courierService.application.aggregates.CourierQueue
import org.pv293.kotlinseminar.courierService.application.commands.impl.CreateDeliveryOfferCommand
import org.pv293.kotlinseminar.courierService.application.services.CourierNotificationService
import org.pv293.kotlinseminar.courierService.repository.CourierQueueRepository
import org.pv293.kotlinseminar.productDeliveryService.events.impl.PackageDroppedByBakerEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Policy that reacts to PackageDroppedByBakerEvent by finding nearby available couriers
 * and creating delivery offers for each of them.
 */
@Component
class CourierNeededPolicy(
    private val courierQueueRepository: CourierQueueRepository,
    private val commandGateway: CommandGateway,
    private val notificationService: CourierNotificationService,
) {
    private val logger = LoggerFactory.getLogger(CourierNeededPolicy::class.java)

    companion object {
        private const val NEARBY_RADIUS_KM = 5.0
    }

    @EventHandler
    fun on(event: PackageDroppedByBakerEvent) {
        logger.info(
            "Package dropped by baker for delivery ${event.deliveryId}, " +
                "finding nearby couriers within ${NEARBY_RADIUS_KM}km..."
        )

        val nearbyCouriers = findCouriersNearLocation(
            event.latitude,
            event.longitude,
            NEARBY_RADIUS_KM,
        )

        if (nearbyCouriers.isEmpty()) {
            logger.info("No available couriers found within ${NEARBY_RADIUS_KM}km of drop location")
            return
        }

        logger.info("Found ${nearbyCouriers.size} nearby courier(s), creating delivery offers...")

        val approxLat = event.latitude.setScale(2, RoundingMode.HALF_UP)
        val approxLon = event.longitude.setScale(2, RoundingMode.HALF_UP)

        for (courier in nearbyCouriers) {
            val offerId = UUID.randomUUID()

            commandGateway.send<Any>(
                CreateDeliveryOfferCommand(
                    offerId = offerId,
                    deliveryId = event.deliveryId,
                    orderId = event.orderId,
                    courierId = courier.courierId,
                    approximateLatitude = approxLat,
                    approximateLongitude = approxLon,
                    droppedAt = event.droppedAt,
                ),
            )

            notificationService.notifyCourierOfDeliveryOffer(
                courierId = courier.courierId,
                offerId = offerId,
                deliveryId = event.deliveryId,
                approximateLatitude = approxLat,
                approximateLongitude = approxLon,
            )

            logger.info("Created delivery offer $offerId for courier ${courier.courierId}")
        }

        logger.info("Finished creating ${nearbyCouriers.size} delivery offer(s) for delivery ${event.deliveryId}")
    }

    private fun findCouriersNearLocation(
        latitude: BigDecimal,
        longitude: BigDecimal,
        radiusKm: Double,
    ): List<CourierQueue> {
        val availableCouriers = courierQueueRepository.findByAvailableTrue()

        return availableCouriers.filter { courier ->
            val distance = calculateDistanceKm(
                latitude,
                longitude,
                courier.latitude,
                courier.longitude,
            )
            distance <= radiusKm
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
