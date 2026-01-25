package org.pv293.kotlinseminar.productDeliveryService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.productDeliveryService.application.dto.DeliveryLocationDTO
import org.pv293.kotlinseminar.productDeliveryService.application.queries.impl.DeliveryLocationQuery
import org.pv293.kotlinseminar.productDeliveryService.repository.DeliveryLocationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DeliveryLocationQueryHandler(
    private val deliveryLocationRepository: DeliveryLocationRepository,
) {
    private val logger = LoggerFactory.getLogger(DeliveryLocationQueryHandler::class.java)

    @QueryHandler
    fun handle(query: DeliveryLocationQuery): DeliveryLocationDTO? {
        logger.info("Handling DeliveryLocationQuery for deliveryId=${query.deliveryId}")

        val location = deliveryLocationRepository.findById(query.deliveryId).orElse(null)

        return location?.let {
            DeliveryLocationDTO(
                deliveryId = it.deliveryId,
                orderId = it.orderId,
                latitude = it.latitude,
                longitude = it.longitude,
                photoUrl = it.photoUrl,
                droppedAt = it.droppedAt,
            )
        }
    }
}
