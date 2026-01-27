package org.pv293.kotlinseminar.courierService.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.courierService.application.commands.impl.AcceptDeliveryOfferCommand
import org.pv293.kotlinseminar.courierService.application.dto.AvailableDeliveryOfferDTO
import org.pv293.kotlinseminar.courierService.application.dto.PackageLocationDTO
import org.pv293.kotlinseminar.courierService.application.queries.impl.AvailableDeliveryOffersQuery
import org.pv293.kotlinseminar.courierService.application.queries.impl.PackageLocationQuery
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

data class AcceptOfferRequest(
    val courierId: String,
)

@RestController
@RequestMapping("/delivery-offers")
@Tag(name = "Delivery Offers", description = "Delivery offer management endpoints")
class DeliveryOfferController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
    private val logger = LoggerFactory.getLogger(DeliveryOfferController::class.java)

    @GetMapping
    @Operation(summary = "List delivery offers, optionally filtered by courier and/or status")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "List of delivery offers"),
        ],
    )
    fun listOffers(
        @RequestParam(required = false) courierId: String?,
        @RequestParam(required = false) status: String?,
    ): ResponseEntity<List<AvailableDeliveryOfferDTO>> {
        logger.info("GET /delivery-offers?courierId=$courierId&status=$status")

        val query = AvailableDeliveryOffersQuery(
            courierId = courierId?.let { UUID.fromString(it) },
            status = status?.uppercase(),
        )

        val offers = queryGateway.query(
            query,
            ResponseTypes.multipleInstancesOf(AvailableDeliveryOfferDTO::class.java),
        ).join()

        return ResponseEntity.ok(offers)
    }

    @GetMapping("/{offerId}")
    @Operation(summary = "Get a specific delivery offer by ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Delivery offer details"),
            ApiResponse(responseCode = "404", description = "Offer not found"),
        ],
    )
    fun getOffer(@PathVariable offerId: String): ResponseEntity<AvailableDeliveryOfferDTO> {
        logger.info("GET /delivery-offers/$offerId")

        val offerUUID = UUID.fromString(offerId)

        val offers = queryGateway.query(
            AvailableDeliveryOffersQuery(),
            ResponseTypes.multipleInstancesOf(AvailableDeliveryOfferDTO::class.java),
        ).join()

        val offer = offers.find { it.offerId == offerUUID }
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(offer)
    }

    @PostMapping("/{offerId}/accept")
    @Operation(summary = "Accept a delivery offer")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Offer accepted successfully"),
            ApiResponse(responseCode = "400", description = "Invalid request or offer cannot be accepted"),
            ApiResponse(responseCode = "404", description = "Offer not found"),
        ],
    )
    fun acceptOffer(
        @PathVariable offerId: String,
        @RequestBody request: AcceptOfferRequest,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("POST /delivery-offers/$offerId/accept by courier ${request.courierId}")

        val offerUUID = UUID.fromString(offerId)
        val courierUUID = UUID.fromString(request.courierId)

        commandGateway.sendAndWait<Any>(
            AcceptDeliveryOfferCommand(
                offerId = offerUUID,
                courierId = courierUUID,
            ),
        )

        return ResponseEntity.ok(
            mapOf(
                "offerId" to offerId,
                "courierId" to request.courierId,
                "status" to "ACCEPTED",
            ),
        )
    }

    @GetMapping("/{offerId}/location")
    @Operation(summary = "Get package location (exact if within 500m, approximate otherwise)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Location retrieved"),
            ApiResponse(responseCode = "403", description = "Courier has not accepted this offer"),
            ApiResponse(responseCode = "404", description = "Offer or location not found"),
        ],
    )
    fun getPackageLocation(
        @PathVariable offerId: String,
        @RequestParam courierId: String,
        @RequestParam lat: BigDecimal,
        @RequestParam lon: BigDecimal,
    ): ResponseEntity<PackageLocationDTO> {
        logger.info("GET /delivery-offers/$offerId/location by courier $courierId at ($lat, $lon)")

        val offerUUID = UUID.fromString(offerId)
        val courierUUID = UUID.fromString(courierId)

        return try {
            val location = queryGateway.query(
                PackageLocationQuery(
                    offerId = offerUUID,
                    courierId = courierUUID,
                    courierLatitude = lat,
                    courierLongitude = lon,
                ),
                PackageLocationDTO::class.java,
            ).join()

            if (location != null) {
                ResponseEntity.ok(location)
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            logger.warn("Access denied: ${e.message}")
            ResponseEntity.status(403).build()
        }
    }
}
