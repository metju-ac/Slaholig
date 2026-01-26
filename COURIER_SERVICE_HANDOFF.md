# Courier Service Implementation - Developer Handoff Document

> **IMPORTANT**: This is a temporary handoff document. Once the remaining features are implemented and the product-delivery-service updates are complete, **DELETE THIS FILE**. It is meant only for implementation guidance and should not remain in the repository long-term.

## Overview

This document tracks the implementation status of the **`courier-service`** microservice and its integration with the **`product-delivery-service`** for package delivery workflow.

## Implementation Status Summary

| Feature | Status |
|---------|--------|
| courier-service module setup | DONE |
| Docker/Prometheus/Grafana integration | DONE |
| Courier availability tracking | DONE |
| Delivery offer creation & notifications | DONE |
| Offer acceptance flow | DONE |
| product-delivery-service integration | DONE |
| Courier pickup (IN_TRANSIT) | TODO |
| Courier drop-off (DROPPED_BY_COURIER) | TODO |
| Delivery confirmation (DELIVERED) | TODO |
| Proximity-based full location access | TODO |

---

## What Has Been Implemented

### 1. courier-service Module (Port 8087)

**Directory Structure:**
```
courier-service/
├── src/main/kotlin/org/pv293/kotlinseminar/courierService/
│   ├── CourierServiceApplication.kt
│   ├── application/
│   │   ├── aggregates/
│   │   │   ├── AnonymizedPackageLocationInfo.kt    # Read model (approx location)
│   │   │   ├── AvailableDeliveryOffer.kt           # Aggregate for offer acceptance
│   │   │   ├── CourierQueue.kt                     # Aggregate for courier availability
│   │   │   ├── OfferStatus.kt                      # PENDING, ACCEPTED
│   │   │   └── PackageLocationInfo.kt              # Read model (full location)
│   │   ├── commands/impl/
│   │   │   ├── AcceptDeliveryOfferCommand.kt
│   │   │   ├── CreateDeliveryOfferCommand.kt
│   │   │   ├── MarkCourierAvailableCommand.kt
│   │   │   ├── MarkCourierUnavailableCommand.kt
│   │   │   └── UpdateCourierLocationCommand.kt
│   │   ├── policies/
│   │   │   └── CourierNeededPolicy.kt              # Finds nearby couriers, creates offers
│   │   ├── queries/handlers/
│   │   │   ├── CourierQueueQueryHandler.kt
│   │   │   └── DeliveryOfferQueryHandler.kt
│   │   └── services/
│   │       └── CourierNotificationService.kt       # Mock email notifications
│   ├── controllers/
│   │   ├── CourierController.kt                    # Courier availability endpoints
│   │   └── DeliveryOfferController.kt              # Delivery offer endpoints
│   ├── events/
│   │   ├── handlers/
│   │   │   └── PackageDroppedByBakerEventHandler.kt
│   │   └── impl/
│   │       └── DeliveryOfferCreatedEvent.kt        # Internal event
│   ├── infrastructure/
│   │   └── AxonConfig.kt
│   └── repository/
│       ├── AnonymizedPackageLocationInfoRepository.kt
│       ├── AvailableDeliveryOfferRepository.kt
│       ├── CourierQueueRepository.kt
│       └── PackageLocationInfoRepository.kt
├── src/main/resources/
│   ├── application-prod.yml
│   ├── application-dev.yml
│   └── logback-spring.xml
├── build.gradle.kts
└── Dockerfile
```

### 2. Shared Module Additions

```
shared/src/main/kotlin/org/pv293/kotlinseminar/courierService/
├── application/
│   ├── dto/
│   │   ├── AvailableCourierDTO.kt
│   │   └── AvailableDeliveryOfferDTO.kt
│   └── queries/impl/
│       ├── AvailableCouriersQuery.kt
│       └── AvailableDeliveryOffersQuery.kt
└── events/impl/
    ├── CourierLocationUpdatedEvent.kt
    ├── CourierMarkedAvailableEvent.kt
    ├── CourierMarkedUnavailableEvent.kt
    └── DeliveryOfferAcceptedEvent.kt           # Cross-service event
```

### 3. product-delivery-service Integration

New event handler:
- `DeliveryOfferAcceptedEventHandler.kt` - Logs when courier accepts a delivery offer

---

## Implemented Features

### Courier Availability Tracking

Couriers can mark themselves as available/unavailable and update their location.

**Aggregate:** `CourierQueue`
- States: available (true/false)
- Tracks: courierId, latitude, longitude, lastUpdatedAt

**REST Endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| PUT | `/couriers/{courierId}/available` | Mark courier available (body: lat, lon) |
| PUT | `/couriers/{courierId}/unavailable` | Mark courier unavailable |
| PUT | `/couriers/{courierId}/location` | Update location while available |
| GET | `/couriers/available` | List all available couriers |
| GET | `/couriers/available/nearby?lat=X&lon=Y&radiusKm=Z` | List nearby couriers |

### CourierNeeded Policy

When a baker drops a package (`PackageDroppedByBakerEvent`), the `CourierNeededPolicy`:
1. Finds all available couriers within **5km** of the drop location
2. Creates an `AvailableDeliveryOffer` for each courier
3. Sends a mock email notification to each courier

**Key Configuration:**
- Radius: 5km (hardcoded in `CourierNeededPolicy.NEARBY_RADIUS_KM`)
- All nearby couriers are notified simultaneously
- First courier to accept gets the delivery

### Delivery Offer Flow

**Aggregate:** `AvailableDeliveryOffer`
- States: `PENDING`, `ACCEPTED`
- Tracks: offerId, deliveryId, orderId, courierId, approximate location, timestamps

**REST Endpoints:**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/delivery-offers` | List offers (filter: courierId, status) |
| GET | `/delivery-offers/{offerId}` | Get specific offer |
| POST | `/delivery-offers/{offerId}/accept` | Accept offer (body: courierId) |

### Location Anonymization

When a package is dropped by baker, two read models are created:
1. **AnonymizedPackageLocationInfo** - Coordinates rounded to 2 decimal places (~100m precision), NO photo URL
2. **PackageLocationInfo** - Full coordinates and photo URL (for later access)

---

## Event Flow (Current State)

```
1. Payment confirmed
   -> PaymentMarkedPaidEvent
   -> product-delivery-service creates PackageDelivery (status: CREATED)

2. Baker drops package
   -> MarkDroppedByBakerCommand
   -> PackageDroppedByBakerEvent (status: DROPPED_BY_BAKER)
   
3. [IMPLEMENTED] courier-service handles PackageDroppedByBakerEvent
   -> PackageDroppedByBakerEventHandler creates read models
   -> CourierNeededPolicy finds nearby couriers
   -> Creates AvailableDeliveryOffer for each courier
   -> Sends mock email notifications

4. [IMPLEMENTED] Courier views and accepts offer
   -> GET /delivery-offers?courierId=...&status=PENDING
   -> POST /delivery-offers/{offerId}/accept
   -> DeliveryOfferAcceptedEvent

5. [IMPLEMENTED] product-delivery-service logs acceptance
   -> DeliveryOfferAcceptedEventHandler logs courier assignment

6. [TODO] Courier gets full location (proximity check required)

7. [TODO] Courier picks up package
   -> MarkPickedUpByCourierCommand -> PackagePickedUpByCourierEvent
   -> Status: IN_TRANSIT

8. [TODO] Courier drops at destination
   -> MarkDroppedByCourierCommand -> PackageDroppedByCourierEvent
   -> Status: DROPPED_BY_COURIER

9. [TODO] Recipient confirms delivery
   -> MarkDeliveredCommand -> PackageDeliveredEvent
   -> Status: DELIVERED
```

---

## What Still Needs To Be Implemented

### 1. Proximity-Based Full Location Access

**Purpose:** Courier should only see exact location + photo when nearby.

**Implementation:**
```kotlin
// courier-service/.../controllers/DeliveryOfferController.kt

@GetMapping("/{offerId}/location")
fun getFullLocation(
    @PathVariable offerId: String,
    @RequestParam courierId: String,
    @RequestParam lat: BigDecimal,
    @RequestParam lon: BigDecimal,
): ResponseEntity<PackageLocationInfoDTO> {
    // 1. Verify offer is ACCEPTED by this courier
    // 2. Check courier location is within X meters of package
    // 3. Return full location with photo URL, or 403
}
```

**Decision needed:** Proximity threshold (suggestion: 500m-1km)

### 2. Courier Pickup Command

**In product-delivery-service:**

```kotlin
// Commands
data class MarkPickedUpByCourierCommand(
    @TargetAggregateIdentifier
    val deliveryId: UUID,
    val courierId: UUID,
)

// Events (in shared module)
data class PackagePickedUpByCourierEvent(
    val deliveryId: UUID,
    val courierId: UUID,
    val pickedUpAt: Instant,
)

// Aggregate handler
@CommandHandler
fun handle(command: MarkPickedUpByCourierCommand) {
    require(status == DeliveryStatus.DROPPED_BY_BAKER) {
        "Package must be dropped by baker before pickup"
    }
    apply(PackagePickedUpByCourierEvent(...))
}

@EventSourcingHandler
fun on(event: PackagePickedUpByCourierEvent) {
    this.status = DeliveryStatus.IN_TRANSIT
    this.courierId = event.courierId
    this.pickedUpAt = event.pickedUpAt
}

// REST endpoint
@PutMapping("/{deliveryId}/pickup")
fun markPickedUp(
    @PathVariable deliveryId: String,
    @RequestBody request: PickupRequest,  // courierId
)
```

### 3. Courier Drop Command

```kotlin
data class MarkDroppedByCourierCommand(
    @TargetAggregateIdentifier
    val deliveryId: UUID,
    val courierId: UUID,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val photoUrl: String,
)

data class PackageDroppedByCourierEvent(
    val deliveryId: UUID,
    val courierId: UUID,
    val droppedAt: Instant,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val photoUrl: String,
)
```

### 4. Delivery Confirmation Command

```kotlin
data class MarkDeliveredCommand(
    @TargetAggregateIdentifier
    val deliveryId: UUID,
    val recipientConfirmation: String?,  // Optional signature/code
)

data class PackageDeliveredEvent(
    val deliveryId: UUID,
    val deliveredAt: Instant,
)
```

### 5. PackageDelivery Aggregate Updates

Add fields to `PackageDelivery.kt`:
```kotlin
var courierId: UUID? = null
var pickedUpAt: Instant? = null
var courierDroppedAt: Instant? = null
var courierDropLatitude: BigDecimal? = null
var courierDropLongitude: BigDecimal? = null
var courierDropPhotoUrl: String? = null
var deliveredAt: Instant? = null
```

---

## Design Decisions Made

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Nearby courier radius | 5km | Reasonable urban delivery distance |
| Courier notification | All at once | Simpler than sequential assignment |
| Offer acceptance | First-come-first-served | AvailableDeliveryOffer status changes to ACCEPTED |
| Coordinate anonymization | 2 decimal places | ~100m precision, protects exact location |
| Offer expiration | Not implemented | Simplicity; can add later if needed |
| Courier location verification | Not implemented | Trust client-reported location for now |
| Courier ID source | Client-provided | No auth system integration yet |

---

## Configuration

| Service | Port (Docker) | Postgres Port |
|---------|---------------|---------------|
| courier-service | 8087 | 5439 |

**Swagger UI:** http://localhost:8087/swagger-ui.html

---

## Testing the Current Implementation

### 1. Start the stack
```bash
docker compose up
```

### 2. Mark a courier as available
```bash
curl -X PUT http://localhost:8087/couriers/11111111-1111-1111-1111-111111111111/available \
  -H "Content-Type: application/json" \
  -d '{"latitude": 49.2, "longitude": 16.6}'
```

### 3. Create and pay for an order (triggers delivery creation)
Use product-selection-service and payment-service endpoints.

### 4. Drop package by baker
```bash
curl -X PUT http://localhost:8086/deliveries/{deliveryId}/drop-by-baker \
  -H "Content-Type: application/json" \
  -d '{"latitude": 49.2, "longitude": 16.6, "photoUrl": "https://example.com/photo.jpg"}'
```

### 5. Check courier received offer
```bash
curl http://localhost:8087/delivery-offers?courierId=11111111-1111-1111-1111-111111111111
```

### 6. Accept the offer
```bash
curl -X POST http://localhost:8087/delivery-offers/{offerId}/accept \
  -H "Content-Type: application/json" \
  -d '{"courierId": "11111111-1111-1111-1111-111111111111"}'
```

---

## Remaining Work Checklist

- [ ] Add `PackagePickedUpByCourierEvent` to shared module
- [ ] Add `PackageDroppedByCourierEvent` to shared module
- [ ] Add `PackageDeliveredEvent` to shared module
- [ ] Update `PackageDelivery` aggregate with new command handlers
- [ ] Add courier-related fields to `PackageDelivery` entity
- [ ] Add REST endpoints for pickup/drop/delivered in product-delivery-service
- [ ] Implement proximity-based full location access in courier-service
- [ ] (Optional) Cancel other pending offers when one is accepted
- [ ] (Optional) Add offer expiration mechanism

---

## Commit History (24 commits)

### Phase 1: Basic Service Setup (Commits 1-8)
1. `build(courier-service): add gradle configuration and project structure`
2. `chore(courier-service): add Spring Boot and logging configuration`
3. `feat(courier-service): implement application class and Axon config`
4. `build(courier-service): add Dockerfile for containerized deployment`
5. `ci(courier-service): integrate with docker-compose and prometheus`
6. `docs(courier-service): add service documentation to README`
7. `feat(courier-service): add JPA entities for location tracking`
8. `feat(courier-service): add PackageDroppedByBakerEventHandler`

### Phase 2: Grafana + Courier Queue (Commits 9-14)
9. `chore(grafana): add courier-service to dashboard`
10. `feat(shared): add CourierQueue events and DTOs`
11. `feat(courier-service): add CourierQueue aggregate and commands`
12. `feat(courier-service): configure CourierQueue aggregate repository`
13. `feat(courier-service): add repository and query handler for CourierQueue`
14. `feat(courier-service): add REST controller for courier availability`

### Phase 3: Delivery Offer Flow (Commits 15-24)
15. `feat(shared): add DeliveryOfferAcceptedEvent`
16. `feat(shared): add AvailableDeliveryOfferDTO and query`
17. `feat(courier-service): add DeliveryOfferCreatedEvent`
18. `feat(courier-service): add AvailableDeliveryOffer aggregate with commands`
19. `feat(courier-service): configure AvailableDeliveryOffer aggregate repository`
20. `feat(courier-service): add CourierNotificationService (mock)`
21. `feat(courier-service): add CourierNeededPolicy event handler`
22. `feat(courier-service): add DeliveryOfferQueryHandler`
23. `feat(courier-service): add DeliveryOfferController endpoints`
24. `feat(product-delivery-service): handle DeliveryOfferAcceptedEvent`

---

## Reference Files

| Purpose | File |
|---------|------|
| CourierNeeded Policy | `courier-service/.../application/policies/CourierNeededPolicy.kt` |
| Offer Aggregate | `courier-service/.../application/aggregates/AvailableDeliveryOffer.kt` |
| Courier Aggregate | `courier-service/.../application/aggregates/CourierQueue.kt` |
| Delivery Controller | `courier-service/.../controllers/DeliveryOfferController.kt` |
| Axon Config | `courier-service/.../infrastructure/AxonConfig.kt` |
| Cross-service Event | `shared/.../courierService/events/impl/DeliveryOfferAcceptedEvent.kt` |

---

## Axon Framework Documentation

Primary docs: https://docs.axoniq.io/home/

Key patterns used:
- State-stored aggregates with JPA (`@Entity` + `@Aggregate`)
- Command handling in aggregates (`@CommandHandler`)
- Event sourcing handlers (`@EventSourcingHandler`)
- External event handlers for cross-service communication (`@EventHandler`)
- Query handlers for read models (`@QueryHandler`)
