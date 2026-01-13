package org.pv293.kotlinseminar.paymentService.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.paymentService.application.commands.impl.PayOrderCommand
import org.pv293.kotlinseminar.paymentService.application.dto.PaymentDTO
import org.pv293.kotlinseminar.paymentService.application.dto.PaymentMethodRequest
import org.pv293.kotlinseminar.paymentService.application.queries.impl.PaymentQuery
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/payments")
class PaymentController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
    private val logger = LoggerFactory.getLogger(PaymentController::class.java)

    @GetMapping("/{orderId}")
    @Operation(summary = "Get payment status by order ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Payment details returned"),
            ApiResponse(responseCode = "404", description = "Payment not found"),
        ],
    )
    fun getPayment(
        @Parameter(example = "11111111-1111-1111-1111-111111111111")
        @PathVariable orderId: String,
    ): PaymentDTO {
        val orderUUID = UUID.fromString(orderId)
        logger.info("Getting payment for order $orderId")
        return queryGateway.query(PaymentQuery(orderUUID), PaymentDTO::class.java).get()
    }

    @PostMapping("/{orderId}/pay")
    @Operation(summary = "Initiate payment for order")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Payment initiated"),
            ApiResponse(responseCode = "400", description = "Payment already processed or in wrong state"),
            ApiResponse(responseCode = "404", description = "Payment not found"),
        ],
    )
    fun payOrder(
        @Parameter(example = "11111111-1111-1111-1111-111111111111")
        @PathVariable orderId: String,
        @RequestBody(required = false) paymentMethod: PaymentMethodRequest?,
    ): PaymentDTO {
        val orderUUID = UUID.fromString(orderId)
        logger.info("Initiating payment for order $orderId with wallet: ${paymentMethod?.walletAddress ?: "not provided"}")

        commandGateway.sendAndWait<Any>(
            PayOrderCommand(
                orderId = orderUUID,
                walletAddress = paymentMethod?.walletAddress,
            ),
        )

        // Wait a bit for async processing to complete
        Thread.sleep(200)

        return queryGateway.query(PaymentQuery(orderUUID), PaymentDTO::class.java).get()
    }
}
