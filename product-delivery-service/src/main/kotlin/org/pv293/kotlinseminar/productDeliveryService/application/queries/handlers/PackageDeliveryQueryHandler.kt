package org.pv293.kotlinseminar.productDeliveryService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.productDeliveryService.application.dto.PackageDeliveryDTO
import org.pv293.kotlinseminar.productDeliveryService.application.queries.impl.PackageDeliveryQuery
import org.pv293.kotlinseminar.productDeliveryService.repository.PackageDeliveryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PackageDeliveryQueryHandler(
    private val packageDeliveryRepository: PackageDeliveryRepository,
) {
    private val logger = LoggerFactory.getLogger(PackageDeliveryQueryHandler::class.java)

    @QueryHandler
    fun handle(query: PackageDeliveryQuery): PackageDeliveryDTO? {
        logger.info("Handling PackageDeliveryQuery: deliveryId=${query.deliveryId}, orderId=${query.orderId}")

        val deliveryId = query.deliveryId
        val orderId = query.orderId

        val delivery = when {
            deliveryId != null -> packageDeliveryRepository.findById(deliveryId).orElse(null)
            orderId != null -> packageDeliveryRepository.findByOrderId(orderId)
            else -> {
                logger.warn("PackageDeliveryQuery must specify either deliveryId or orderId")
                null
            }
        }

        return delivery?.let {
            PackageDeliveryDTO(
                deliveryId = it.deliveryId,
                orderId = it.orderId,
                transactionId = it.transactionId,
                status = it.status.name,
                createdAt = it.createdAt,
            )
        }
    }
}
