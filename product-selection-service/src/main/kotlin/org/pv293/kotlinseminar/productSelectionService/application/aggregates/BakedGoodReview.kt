package org.pv293.kotlinseminar.productSelectionService.application.aggregates

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.axonframework.modelling.command.EntityId
import java.util.UUID

@Entity
@Table(name = "baked_good_review")
class BakedGoodReview {

    @Id
    @EntityId
    lateinit var id: UUID

    @Column(name = "author_id")
    lateinit var authorId: UUID

    @Column(name = "rating")
    var rating: Int = 0

    @Column(name = "content", length = 1000)
    var content: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baked_good_id")
    var bakedGood: BakedGood? = null
}
