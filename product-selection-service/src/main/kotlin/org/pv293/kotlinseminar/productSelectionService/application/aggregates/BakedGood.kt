package org.pv293.kotlinseminar.productSelectionService.application.aggregates

import jakarta.persistence.Column
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.modelling.command.ForwardMatchingInstances
import org.axonframework.spring.stereotype.Aggregate
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.AddBakedGoodsReviewCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.PublishBakedGoodsCommand
import org.pv293.kotlinseminar.productSelectionService.application.commands.impl.RestockBakedGoodsCommand
import org.pv293.kotlinseminar.productSelectionService.events.impl.BakedGoodsPublishedEvent
import org.pv293.kotlinseminar.productSelectionService.events.impl.BakedGoodsReviewAddedEvent
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

    @Column(name = "latitude")
    var latitude: Double = 0.0

    @Column(name = "longitude")
    var longitude: Double = 0.0

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "bakedGood", cascade = [CascadeType.ALL])
    @AggregateMember(eventForwardingMode = ForwardMatchingInstances::class)
    open var reviews: MutableList<BakedGoodReview> = mutableListOf()

    @CommandHandler
    constructor(command: PublishBakedGoodsCommand) : this() {
        require(command.initialStock >= 0) { "Initial stock must be >= 0" }

        id = command.id
        name = command.name
        description = command.description
        stock = command.initialStock
        latitude = command.latitude
        longitude = command.longitude

        apply(
            BakedGoodsPublishedEvent(
                bakedGoodsId = command.id,
                name = command.name,
                description = command.description,
                initialStock = command.initialStock,
                latitude = command.latitude,
                longitude = command.longitude,
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

    @CommandHandler
    fun handle(command: AddBakedGoodsReviewCommand) {
        require(command.rating in 1..5) { "Rating must be between 1 and 5" }

        val review = BakedGoodReview().apply {
            id = command.reviewId
            authorId = command.authorId
            rating = command.rating
            content = command.content
            bakedGood = this@BakedGood
        }

        reviews.add(review)
        apply(
            BakedGoodsReviewAddedEvent(
                bakedGoodsId = id,
                reviewId = command.reviewId,
                authorId = command.authorId,
                rating = command.rating,
                content = command.content,
            ),
        )
    }
}
