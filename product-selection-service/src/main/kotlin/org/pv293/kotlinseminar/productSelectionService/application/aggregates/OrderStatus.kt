package org.pv293.kotlinseminar.productSelectionService.application.aggregates

enum class OrderStatus {
    CREATED,
    PAYMENT_PROCESSING,
    PAID,
    RELEASED,
    FAILED,
}
