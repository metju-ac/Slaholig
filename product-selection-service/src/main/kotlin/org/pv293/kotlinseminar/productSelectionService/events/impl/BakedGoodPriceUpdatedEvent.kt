package org.pv293.kotlinseminar.productSelectionService.events.impl

import java.math.BigDecimal
import java.util.UUID

data class BakedGoodPriceUpdatedEvent(
    val bakedGoodsId: UUID,
    val oldPrice: BigDecimal,
    val newPrice: BigDecimal,
)
