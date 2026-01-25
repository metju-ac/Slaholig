package org.pv293.kotlinseminar.productSelectionService.application.aggregates

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "order_item")
data class OrderItem(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "baked_goods_id")
    val bakedGoodsId: UUID,

    @Column(name = "baked_goods_name", length = 100)
    val bakedGoodsName: String,

    @Column(name = "quantity")
    val quantity: Int,

    @Column(name = "price_per_unit", precision = 19, scale = 2)
    val pricePerUnit: BigDecimal,

    @Column(name = "total_price", precision = 19, scale = 2)
    val totalPrice: BigDecimal,

    @ManyToOne
    @JoinColumn(name = "order_id")
    var order: Order? = null,
)
