package org.pv293.kotlinseminar.courierService.events.handlers

import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.courierService.application.projections.CourierQueueProjection
import org.pv293.kotlinseminar.courierService.events.impl.CourierLocationUpdatedEvent
import org.pv293.kotlinseminar.courierService.events.impl.CourierMarkedAvailableEvent
import org.pv293.kotlinseminar.courierService.events.impl.CourierMarkedUnavailableEvent
import org.pv293.kotlinseminar.courierService.repository.CourierQueueProjectionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Event handler that maintains the courier queue projection (read model).
 */
@Component
class CourierQueueProjectionEventHandler(
    private val courierQueueProjectionRepository: CourierQueueProjectionRepository,
) {
    private val logger = LoggerFactory.getLogger(CourierQueueProjectionEventHandler::class.java)

    @EventHandler
    fun on(event: CourierMarkedAvailableEvent) {
        logger.info("Updating projection: Courier ${event.courierId} marked available")

        val projection = CourierQueueProjection(
            courierId = event.courierId,
            latitude = event.latitude,
            longitude = event.longitude,
            available = true,
            lastUpdatedAt = event.availableAt,
        )

        courierQueueProjectionRepository.save(projection)
        logger.info("Projection updated: Courier ${event.courierId} is now available at (${event.latitude}, ${event.longitude})")
    }

    @EventHandler
    fun on(event: CourierMarkedUnavailableEvent) {
        logger.info("Updating projection: Courier ${event.courierId} marked unavailable")

        courierQueueProjectionRepository.findById(event.courierId).ifPresent { projection ->
            projection.available = false
            projection.lastUpdatedAt = event.unavailableAt
            courierQueueProjectionRepository.save(projection)
            logger.info("Projection updated: Courier ${event.courierId} is now unavailable")
        }
    }

    @EventHandler
    fun on(event: CourierLocationUpdatedEvent) {
        logger.info("Updating projection: Courier ${event.courierId} location updated")

        courierQueueProjectionRepository.findById(event.courierId).ifPresent { projection ->
            projection.latitude = event.latitude
            projection.longitude = event.longitude
            projection.lastUpdatedAt = event.updatedAt
            courierQueueProjectionRepository.save(projection)
            logger.info("Projection updated: Courier ${event.courierId} location updated to (${event.latitude}, ${event.longitude})")
        }
    }
}
