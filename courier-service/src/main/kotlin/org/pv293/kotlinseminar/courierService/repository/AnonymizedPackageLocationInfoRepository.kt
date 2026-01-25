package org.pv293.kotlinseminar.courierService.repository

import org.pv293.kotlinseminar.courierService.application.aggregates.AnonymizedPackageLocationInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AnonymizedPackageLocationInfoRepository : JpaRepository<AnonymizedPackageLocationInfo, UUID> {
    fun findByAvailableTrue(): List<AnonymizedPackageLocationInfo>
}
