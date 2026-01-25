package org.pv293.kotlinseminar.productSelectionService.application.aggregates

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.CascadeType
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "order_projection")
data class Order(
    @Id
    val orderId: UUID,

    @Column(name = "cart_id")
    val cartId: UUID,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "subtotal", precision = 19, scale = 2)
    val subtotal: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: OrderStatus = OrderStatus.CREATED,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    val items: MutableList<OrderItem> = mutableListOf(),
)
