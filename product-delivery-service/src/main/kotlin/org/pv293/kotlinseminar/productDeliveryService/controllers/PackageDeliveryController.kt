package org.pv293.kotlinseminar.productDeliveryService.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.productDeliveryService.application.commands.impl.MarkDroppedByBakerCommand
import org.pv293.kotlinseminar.productDeliveryService.application.dto.DeliveryLocationDTO
import org.pv293.kotlinseminar.productDeliveryService.application.dto.PackageDeliveryDTO
import org.pv293.kotlinseminar.productDeliveryService.application.queries.impl.DeliveryLocationQuery
import org.pv293.kotlinseminar.productDeliveryService.application.queries.impl.PackageDeliveryQuery
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

data class DropPackageRequest(
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val photoUrl: String,
)

@RestController
@RequestMapping("/deliveries")
@Tag(name = "Package Delivery", description = "Package delivery tracking endpoints")
class PackageDeliveryController(
    private val queryGateway: QueryGateway,
    private val commandGateway: CommandGateway,
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

    @PutMapping("/{deliveryId}/drop-by-baker")
    @Operation(summary = "Mark package as dropped by baker at dead drop location")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Package dropped successfully"),
            ApiResponse(responseCode = "400", description = "Package already dropped or invalid status"),
            ApiResponse(responseCode = "404", description = "Package delivery not found"),
        ],
    )
    fun dropPackageByBaker(
        @PathVariable deliveryId: String,
        @RequestBody request: DropPackageRequest,
    ): ResponseEntity<PackageDeliveryDTO> {
        logger.info("PUT /deliveries/{}/drop-by-baker at ({}, {})", deliveryId, request.latitude, request.longitude)

        val deliveryUUID = UUID.fromString(deliveryId)

        commandGateway.sendAndWait<Any>(
            MarkDroppedByBakerCommand(
                deliveryId = deliveryUUID,
                latitude = request.latitude,
                longitude = request.longitude,
                photoUrl = request.photoUrl,
            ),
        )

        // Query updated state
        val delivery = queryGateway.query(
            PackageDeliveryQuery(deliveryId = deliveryUUID),
            PackageDeliveryDTO::class.java,
        ).join()

        return ResponseEntity.ok(delivery)
    }

    @GetMapping("/{deliveryId}/location")
    @Operation(summary = "Get drop location for package (for couriers)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Location found"),
            ApiResponse(responseCode = "404", description = "Location not found (package not dropped yet)"),
        ],
    )
    fun getDeliveryLocation(@PathVariable deliveryId: String): ResponseEntity<DeliveryLocationDTO> {
        logger.info("GET /deliveries/{}/location", deliveryId)

        val location = queryGateway.query(
            DeliveryLocationQuery(deliveryId = UUID.fromString(deliveryId)),
            DeliveryLocationDTO::class.java,
        ).join()

        return if (location != null) {
            ResponseEntity.ok(location)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
