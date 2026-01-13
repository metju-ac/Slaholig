package org.pv293.kotlinseminar.productSelectionService.application.aggregates

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.spring.stereotype.Aggregate
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.ChooseLocationCommand
import org.pv293.kotlinseminar.productSelectionService.events.impl.LocationChosenEvent
import java.math.BigDecimal
import java.util.UUID

@Entity
@Aggregate(repository = "chosenLocationAggregateRepository")
@Table(name = "chosen_location")
class ChosenLocation() {

    @Id
    @AggregateIdentifier
    lateinit var id: UUID

    @Column(name = "latitude", precision = 10, scale = 7)
    var latitude: BigDecimal = BigDecimal.ZERO

    @Column(name = "longitude", precision = 10, scale = 7)
    var longitude: BigDecimal = BigDecimal.ZERO

    @CommandHandler
    constructor(command: ChooseLocationCommand) : this() {
        id = command.locationId
        latitude = command.latitude
        longitude = command.longitude

        apply(
            LocationChosenEvent(
                locationId = command.locationId,
                latitude = command.latitude,
                longitude = command.longitude,
            ),
        )
    }
}
