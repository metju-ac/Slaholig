package org.pv293.kotlinseminar.courierService.repository

import org.pv293.kotlinseminar.courierService.application.aggregates.CourierQueue
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CourierQueueRepository : JpaRepository<CourierQueue, UUID> {
    fun findByAvailableTrue(): List<CourierQueue>
}
