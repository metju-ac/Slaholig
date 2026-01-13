package org.pv293.kotlinseminar.courierService.application.queries.handlers

import org.axonframework.queryhandling.QueryHandler
import org.pv293.kotlinseminar.courierService.application.aggregates.OfferStatus
import org.pv293.kotlinseminar.courierService.application.dto.AvailableDeliveryOfferDTO
import org.pv293.kotlinseminar.courierService.application.queries.impl.AvailableDeliveryOffersQuery
import org.pv293.kotlinseminar.courierService.repository.AvailableDeliveryOfferRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DeliveryOfferQueryHandler(
    private val deliveryOfferRepository: AvailableDeliveryOfferRepository,
) {
    private val logger = LoggerFactory.getLogger(DeliveryOfferQueryHandler::class.java)

    @QueryHandler
    fun handle(query: AvailableDeliveryOffersQuery): List<AvailableDeliveryOfferDTO> {
        logger.info("Handling AvailableDeliveryOffersQuery: courierId=${query.courierId}, status=${query.status}")

        val courierId = query.courierId
        val statusStr = query.status

        val offers = when {
            courierId != null && statusStr != null -> {
                val status = OfferStatus.valueOf(statusStr)
                deliveryOfferRepository.findByCourierIdAndStatus(courierId, status)
            }
            courierId != null -> {
                deliveryOfferRepository.findByCourierId(courierId)
            }
            statusStr != null -> {
                val status = OfferStatus.valueOf(statusStr)
                deliveryOfferRepository.findByStatus(status)
            }
            else -> {
                deliveryOfferRepository.findAll()
            }
        }

        return offers.map { offer ->
            AvailableDeliveryOfferDTO(
                offerId = offer.offerId,
                deliveryId = offer.deliveryId,
                orderId = offer.orderId,
                courierId = offer.courierId,
                approximateLatitude = offer.approximateLatitude,
                approximateLongitude = offer.approximateLongitude,
                droppedAt = offer.droppedAt,
                offeredAt = offer.offeredAt,
                status = offer.status.name,
            )
        }
    }
}
