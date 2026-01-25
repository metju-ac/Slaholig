package org.pv293.kotlinseminar.productDeliveryService.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.productDeliveryService.application.dto.PackageDeliveryDTO
import org.pv293.kotlinseminar.productDeliveryService.application.queries.impl.PackageDeliveryQuery
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/deliveries")
@Tag(name = "Package Delivery", description = "Package delivery tracking endpoints")
class PackageDeliveryController(
    private val queryGateway: QueryGateway,
) {
    private val logger = LoggerFactory.getLogger(PackageDeliveryController::class.java)

    @GetMapping("/{deliveryId}")
    @Operation(summary = "Get delivery by delivery ID")
    fun getDeliveryById(@PathVariable deliveryId: String): ResponseEntity<PackageDeliveryDTO> {
        logger.info("GET /deliveries/{}", deliveryId)

        val delivery = queryGateway.query(
            PackageDeliveryQuery(deliveryId = UUID.fromString(deliveryId)),
            PackageDeliveryDTO::class.java,
        ).join()

        return if (delivery != null) {
            ResponseEntity.ok(delivery)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get delivery by order ID")
    fun getDeliveryByOrderId(@PathVariable orderId: String): ResponseEntity<PackageDeliveryDTO> {
        logger.info("GET /deliveries/order/{}", orderId)

        val delivery = queryGateway.query(
            PackageDeliveryQuery(orderId = UUID.fromString(orderId)),
            PackageDeliveryDTO::class.java,
        ).join()

        return if (delivery != null) {
            ResponseEntity.ok(delivery)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
