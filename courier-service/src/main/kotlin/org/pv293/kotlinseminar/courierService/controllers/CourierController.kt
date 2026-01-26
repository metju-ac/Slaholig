package org.pv293.kotlinseminar.courierService.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.pv293.kotlinseminar.courierService.application.commands.impl.MarkCourierAvailableCommand
import org.pv293.kotlinseminar.courierService.application.commands.impl.MarkCourierUnavailableCommand
import org.pv293.kotlinseminar.courierService.application.commands.impl.UpdateCourierLocationCommand
import org.pv293.kotlinseminar.courierService.application.dto.AvailableCourierDTO
import org.pv293.kotlinseminar.courierService.application.queries.impl.AvailableCouriersQuery
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

data class CourierLocationRequest(
    val latitude: BigDecimal,
    val longitude: BigDecimal,
)

@RestController
@RequestMapping("/couriers")
@Tag(name = "Courier Availability", description = "Courier availability and location management endpoints")
class CourierController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
    private val logger = LoggerFactory.getLogger(CourierController::class.java)

    @PutMapping("/{courierId}/available")
    @Operation(summary = "Mark courier as available with current location")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Courier marked as available"),
            ApiResponse(responseCode = "400", description = "Invalid request"),
        ],
    )
    fun markAvailable(
        @PathVariable courierId: String,
        @RequestBody request: CourierLocationRequest,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("PUT /couriers/{}/available at ({}, {})", courierId, request.latitude, request.longitude)

        val courierUUID = UUID.fromString(courierId)

        commandGateway.sendAndWait<Any>(
            MarkCourierAvailableCommand(
                courierId = courierUUID,
                latitude = request.latitude,
                longitude = request.longitude,
            ),
        )

        return ResponseEntity.ok(
            mapOf(
                "courierId" to courierId,
                "available" to true,
                "latitude" to request.latitude,
                "longitude" to request.longitude,
            ),
        )
    }

    @PutMapping("/{courierId}/unavailable")
    @Operation(summary = "Mark courier as unavailable")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Courier marked as unavailable"),
            ApiResponse(responseCode = "404", description = "Courier not found"),
        ],
    )
    fun markUnavailable(@PathVariable courierId: String): ResponseEntity<Map<String, Any>> {
        logger.info("PUT /couriers/{}/unavailable", courierId)

        val courierUUID = UUID.fromString(courierId)

        commandGateway.sendAndWait<Any>(
            MarkCourierUnavailableCommand(courierId = courierUUID),
        )

        return ResponseEntity.ok(
            mapOf(
                "courierId" to courierId,
                "available" to false,
            ),
        )
    }

    @PutMapping("/{courierId}/location")
    @Operation(summary = "Update courier location while available")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Location updated"),
            ApiResponse(responseCode = "400", description = "Courier not available"),
            ApiResponse(responseCode = "404", description = "Courier not found"),
        ],
    )
    fun updateLocation(
        @PathVariable courierId: String,
        @RequestBody request: CourierLocationRequest,
    ): ResponseEntity<Map<String, Any>> {
        logger.info("PUT /couriers/{}/location at ({}, {})", courierId, request.latitude, request.longitude)

        val courierUUID = UUID.fromString(courierId)

        commandGateway.sendAndWait<Any>(
            UpdateCourierLocationCommand(
                courierId = courierUUID,
                latitude = request.latitude,
                longitude = request.longitude,
            ),
        )

        return ResponseEntity.ok(
            mapOf(
                "courierId" to courierId,
                "latitude" to request.latitude,
                "longitude" to request.longitude,
            ),
        )
    }

    @GetMapping("/available")
    @Operation(summary = "List all available couriers")
    fun listAvailable(): ResponseEntity<List<AvailableCourierDTO>> {
        logger.info("GET /couriers/available")

        val couriers = queryGateway.query(
            AvailableCouriersQuery(),
            ResponseTypes.multipleInstancesOf(AvailableCourierDTO::class.java),
        ).join()

        return ResponseEntity.ok(couriers)
    }

    @GetMapping("/available/nearby")
    @Operation(summary = "List available couriers within a radius from a location")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "List of nearby available couriers"),
            ApiResponse(responseCode = "400", description = "Missing required parameters"),
        ],
    )
    fun listAvailableNearby(
        @RequestParam lat: BigDecimal,
        @RequestParam lon: BigDecimal,
        @RequestParam radiusKm: Double,
    ): ResponseEntity<List<AvailableCourierDTO>> {
        logger.info("GET /couriers/available/nearby?lat={}&lon={}&radiusKm={}", lat, lon, radiusKm)

        val couriers = queryGateway.query(
            AvailableCouriersQuery(
                nearLatitude = lat,
                nearLongitude = lon,
                radiusKm = radiusKm,
            ),
            ResponseTypes.multipleInstancesOf(AvailableCourierDTO::class.java),
        ).join()

        return ResponseEntity.ok(couriers)
    }
}
