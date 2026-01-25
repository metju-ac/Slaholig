package org.pv293.kotlinseminar.paymentService.application.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.random.Random

data class PaymentGatewayResult(
    val success: Boolean,
    val transactionId: String?,
    val errorMessage: String?,
)

@Service
class CryptoPaymentGatewayService {
    private val logger = LoggerFactory.getLogger(CryptoPaymentGatewayService::class.java)

    fun mockProcessPayment(orderId: UUID, itemCount: Int): PaymentGatewayResult {
        logger.info("Calling crypto payment gateway for order $orderId with $itemCount items")

        // Simulate network delay
        Thread.sleep(100)

        // Random 80% success, 20% failure
        val isSuccess = Random.nextFloat() < 0.8

        return if (isSuccess) {
            val transactionId = "CRYPTO-TX-${UUID.randomUUID()}"
            logger.info("Crypto payment succeeded for order $orderId. Transaction ID: $transactionId")
            PaymentGatewayResult(
                success = true,
                transactionId = transactionId,
                errorMessage = null,
            )
        } else {
            val errorMessage = "Insufficient funds in crypto wallet"
            logger.error("Crypto payment failed for order $orderId: $errorMessage")
            PaymentGatewayResult(
                success = false,
                transactionId = null,
                errorMessage = errorMessage,
            )
        }
    }
}
