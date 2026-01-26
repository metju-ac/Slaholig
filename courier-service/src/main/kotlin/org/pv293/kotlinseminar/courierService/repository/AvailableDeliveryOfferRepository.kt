package org.pv293.kotlinseminar.courierService.repository

import org.pv293.kotlinseminar.courierService.application.aggregates.AvailableDeliveryOffer
import org.pv293.kotlinseminar.courierService.application.aggregates.OfferStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AvailableDeliveryOfferRepository : JpaRepository<AvailableDeliveryOffer, UUID> {
    fun findByCourierId(courierId: UUID): List<AvailableDeliveryOffer>
    fun findByStatus(status: OfferStatus): List<AvailableDeliveryOffer>
    fun findByCourierIdAndStatus(courierId: UUID, status: OfferStatus): List<AvailableDeliveryOffer>
}
