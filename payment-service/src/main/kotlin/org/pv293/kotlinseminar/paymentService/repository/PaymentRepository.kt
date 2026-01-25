package org.pv293.kotlinseminar.paymentService.repository

import org.pv293.kotlinseminar.paymentService.application.aggregates.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PaymentRepository : JpaRepository<Payment, UUID>
