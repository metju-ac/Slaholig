package org.pv293.kotlinseminar.courierService.repository

import org.pv293.kotlinseminar.courierService.application.projections.CourierQueueProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CourierQueueProjectionRepository : JpaRepository<CourierQueueProjection, UUID> {
    fun findByAvailableTrue(): List<CourierQueueProjection>
}
