package org.pv293.kotlinseminar.courierService.events.handlers

import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.courierService.application.aggregates.AnonymizedPackageLocationInfo
import org.pv293.kotlinseminar.courierService.application.aggregates.PackageLocationInfo
import org.pv293.kotlinseminar.courierService.repository.AnonymizedPackageLocationInfoRepository
import org.pv293.kotlinseminar.courierService.repository.PackageLocationInfoRepository
import org.pv293.kotlinseminar.productDeliveryService.events.impl.PackageDroppedByBakerEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.RoundingMode

@Component
class PackageDroppedByBakerEventHandler(
    private val anonymizedLocationRepository: AnonymizedPackageLocationInfoRepository,
    private val fullLocationRepository: PackageLocationInfoRepository,
) {
    private val logger = LoggerFactory.getLogger(PackageDroppedByBakerEventHandler::class.java)

    @EventHandler
    fun on(event: PackageDroppedByBakerEvent) {
        logger.info("Received PackageDroppedByBakerEvent for delivery ${event.deliveryId}")

        // Create anonymized location with reduced precision (~100m accuracy)
        val anonymizedLocation = AnonymizedPackageLocationInfo(
            deliveryId = event.deliveryId,
            orderId = event.orderId,
            approximateLatitude = event.latitude.setScale(2, RoundingMode.HALF_UP),
            approximateLongitude = event.longitude.setScale(2, RoundingMode.HALF_UP),
            droppedAt = event.droppedAt,
            available = true,
        )
        anonymizedLocationRepository.save(anonymizedLocation)

        // Store full location info (only accessible after acceptance + proximity check)
        val fullLocation = PackageLocationInfo(
            deliveryId = event.deliveryId,
            orderId = event.orderId,
            latitude = event.latitude,
            longitude = event.longitude,
            photoUrl = event.photoUrl,
            droppedAt = event.droppedAt,
        )
        fullLocationRepository.save(fullLocation)

        logger.info(
            "Created delivery offer for delivery ${event.deliveryId} " +
                "at approximate location (${anonymizedLocation.approximateLatitude}, ${anonymizedLocation.approximateLongitude})"
        )
    }
}
