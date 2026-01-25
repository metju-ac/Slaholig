package org.pv293.kotlinseminar.productDeliveryService.repository

import org.pv293.kotlinseminar.productDeliveryService.application.aggregates.DeliveryLocation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DeliveryLocationRepository : JpaRepository<DeliveryLocation, UUID>
