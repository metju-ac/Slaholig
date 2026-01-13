package org.pv293.kotlinseminar.productSelectionService.repository

import org.pv293.kotlinseminar.productSelectionService.application.aggregates.ChosenLocation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ChosenLocationRepository : JpaRepository<ChosenLocation, UUID>
