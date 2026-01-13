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
@Table(name = "shopping_cart_item")
class ShoppingCartItem {

    @Id
    @EntityId
    lateinit var id: UUID

    @Column(name = "baked_goods_id")
    lateinit var bakedGoodsId: UUID

    @Column(name = "quantity")
    var quantity: Int = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    var cart: ShoppingCart? = null
}
