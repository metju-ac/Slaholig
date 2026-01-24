package org.pv293.kotlinseminar.productSelectionService.application.aggregates

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.spring.stereotype.Aggregate
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.PublishBakedGoodsCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.RestockBakedGoodsCommand
import org.pv293.kotlinseminar.productSelectionService.events.impl.BakedGoodsPublishedEvent
import org.pv293.kotlinseminar.productSelectionService.events.impl.BakedGoodsRestockedEvent
import java.util.UUID

@Entity
@Aggregate(repository = "bakedGoodAggregateRepository")
@Table(name = "baked_good")
class BakedGood() {

    @Id
    @AggregateIdentifier
    lateinit var id: UUID

    @Column(name = "name", length = 100)
    lateinit var name: String

    @Column(name = "description", length = 500)
    var description: String? = null

    @Column(name = "stock")
    var stock: Int = 0

    @CommandHandler
    constructor(command: PublishBakedGoodsCommand) : this() {
        require(command.initialStock >= 0) { "Initial stock must be >= 0" }

        id = command.id
        name = command.name
        description = command.description
        stock = command.initialStock

        apply(
            BakedGoodsPublishedEvent(
                bakedGoodsId = command.id,
                name = command.name,
                description = command.description,
                initialStock = command.initialStock,
            ),
        )
    }

    @CommandHandler
    fun handle(command: RestockBakedGoodsCommand) {
        require(command.amount > 0) { "Restock amount must be > 0" }

        stock += command.amount
        apply(
            BakedGoodsRestockedEvent(
                bakedGoodsId = id,
                amount = command.amount,
                newStock = stock,
            ),
        )
    }
}
