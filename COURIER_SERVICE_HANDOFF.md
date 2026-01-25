# Courier Service Implementation - Developer Handoff Document

> **IMPORTANT**: This is a temporary handoff document. Once the courier-service has been implemented and the product-delivery-service updates are complete, **DELETE THIS FILE**. It is meant only for implementation guidance and should not remain in the repository long-term.

## Overview

This document provides guidance for implementing a new **`courier-service`** microservice and updating the existing **`product-delivery-service`** to support the courier workflow for package delivery.

## Current State: What Has Been Implemented

### product-delivery-service (Existing)

The `product-delivery-service` currently handles:

1. **Package Delivery Aggregate** (`PackageDelivery`)
   - Tracks delivery lifecycle with states: `CREATED`, `DROPPED_BY_BAKER`, `IN_TRANSIT`, `DROPPED_BY_COURIER`, `DELIVERED`
   - Location: `product-delivery-service/src/main/kotlin/org/pv293/kotlinseminar/productDeliveryService/application/aggregates/PackageDelivery.kt:22-28`
   - Stores package metadata: deliveryId, orderId, transactionId, location coordinates, photo URL
   - Currently handles: package creation and baker drop-off

2. **DeliveryLocation Read Model**
   - Full location information including exact coordinates and photo URL
   - Location: `product-delivery-service/src/main/kotlin/org/pv293/kotlinseminar/productDeliveryService/application/aggregates/DeliveryLocation.kt:13-31`
   - Contains: latitude, longitude, photoUrl, droppedAt timestamp

3. **Event Handlers**
   - `PaymentMarkedPaidEventHandler`: Creates package delivery when payment is confirmed
   - `PackageDroppedByBakerEventHandler`: Updates location read model when baker drops package
   - Location: `product-delivery-service/src/main/kotlin/org/pv293/kotlinseminar/productDeliveryService/events/handlers/`

4. **Events Published** (in `shared` module)
   - `PackageDeliveryCreatedEvent`: When delivery record is created
   - `PackageDroppedByBakerEvent`: When baker drops package at dead drop location
     - Contains: deliveryId, orderId, droppedAt, latitude, longitude, photoUrl
   - Location: `shared/src/main/kotlin/org/pv293/kotlinseminar/productDeliveryService/events/impl/`

5. **REST Endpoints**
   - `GET /deliveries/{deliveryId}` - Get delivery info
   - `GET /deliveries/order/{orderId}` - Get delivery by order
   - `PUT /deliveries/{deliveryId}/drop-by-baker` - Baker marks package dropped
   - `GET /deliveries/{deliveryId}/location` - Get full location details
   - Port: 8086 (via docker-compose)

### Shared Module (Existing)

Contains cross-service contracts:
- DTOs: `PackageDeliveryDTO`, `DeliveryLocationDTO`
- Events: `PackageDroppedByBakerEvent`, `PackageDeliveryCreatedEvent`
- Queries: `PackageDeliveryQuery`, `DeliveryLocationQuery`

## What Needs To Be Implemented

### 1. New courier-service Module

Create a new microservice by copying the structure from existing services (e.g., `product-delivery-service` or `payment-service`).

#### Module Setup

1. **Directory Structure**: Copy from any existing service
   ```
   courier-service/
   ├── src/main/kotlin/org/pv293/kotlinseminar/courierService/
   │   ├── CourierServiceApplication.kt
   │   ├── application/
   │   │   ├── aggregates/          # Courier aggregate
   │   │   ├── commands/impl/       # AcceptDeliveryOfferCommand, etc.
   │   │   ├── queries/handlers/    # Query handlers
   │   │   └── services/            # Business logic
   │   ├── controllers/             # REST endpoints
   │   ├── events/handlers/         # Event handlers (for PackageDroppedByBakerEvent)
   │   ├── infrastructure/          # AxonConfig
   │   └── repository/              # JPA repositories
   ├── src/main/resources/
   │   ├── application-prod.yml     # Postgres connection, Axon config
   │   ├── application-dev.yml      # H2/local config
   │   └── logback-spring.xml       # Loki logging
   ├── build.gradle.kts             # Same deps as other services
   └── Dockerfile                   # Multi-stage build
   ```

2. **Gradle Configuration**
   - Add `include("courier-service")` to `settings.gradle.kts:10`
   - Copy `build.gradle.kts` from `product-delivery-service` or `payment-service`
   - Update `mainClass` in BootJar task to: `org.pv293.kotlinseminar.courierService.CourierServiceApplicationKt`

3. **Docker Compose Configuration**
   - Add Postgres container `postgres-courier` on port `5439`
   - Add service definition (port `8087` recommended)
   - Update `prometheus.yml` with courier-service scrape config

#### Core Functionality: PackageDroppedByBakerEvent Handler

The courier service must listen for `PackageDroppedByBakerEvent` from product-delivery-service.

**Key Responsibilities:**
- Create read models for available delivery offers
- Store **anonymized location info** (approximate coordinates, NO photo)
- Make offers visible to couriers
- Track which couriers have accepted which deliveries

**Implementation Pattern:**
```kotlin
@Component
class PackageDroppedByBakerEventHandler(
    // Inject repositories for read models
) {
    @EventHandler
    fun on(event: PackageDroppedByBakerEvent) {
        // 1. Create AnonymizedPackageLocationInfo read model
        //    - Approximate/fuzzy coordinates (reduce precision)
        //    - NO photoUrl field
        //    - Mark as available for courier acceptance
        
        // 2. Store full location reference (deliveryId, orderId)
        //    but don't expose coordinates/photo yet
        
        // 3. Log that new delivery offer is available
    }
}
```

#### Read Models Required

1. **AnonymizedPackageLocationInfo** (JPA Entity)
   - Fields: deliveryId, orderId, approximateLatitude, approximateLongitude, droppedAt
   - Purpose: Show couriers general area without exact location or photo
   - Precision: Round coordinates to ~100m accuracy (e.g., 2-3 decimal places instead of 7)

2. **PackageLocationInfo** (JPA Entity)
   - Fields: deliveryId, orderId, latitude, longitude, photoUrl, droppedAt
   - Purpose: Full location details for accepted couriers who are nearby
   - Initially populated but NOT exposed to couriers until conditions met

3. **CourierDeliveryAcceptance** (Aggregate or Entity)
   - Track which courier accepted which delivery
   - Fields: acceptanceId, courierId, deliveryId, acceptedAt, courierLatitude, courierLongitude
   - State management for delivery lifecycle from courier perspective

### 2. Updates to product-delivery-service

The existing service needs to be extended to handle courier interactions.

#### New Commands

1. **MarkPickedUpByCourierCommand**
   - deliveryId, courierId, pickedUpAt
   - Transitions: `DROPPED_BY_BAKER` → `IN_TRANSIT`

2. **MarkDroppedByCourierCommand**
   - deliveryId, courierId, latitude, longitude, photoUrl, droppedAt
   - Transitions: `IN_TRANSIT` → `DROPPED_BY_COURIER`

3. **MarkDeliveredCommand**
   - deliveryId, recipientId, deliveredAt
   - Transitions: `DROPPED_BY_COURIER` → `DELIVERED`

#### Aggregate Updates

Update `PackageDelivery.kt:82-108` aggregate to add command handlers:

```kotlin
@CommandHandler
fun handle(command: MarkPickedUpByCourierCommand) {
    require(status == DeliveryStatus.DROPPED_BY_BAKER) {
        "Package must be dropped by baker before pickup. Current: $status"
    }
    apply(PackagePickedUpByCourierEvent(...))
}

@EventSourcingHandler
fun on(event: PackagePickedUpByCourierEvent) {
    this.status = DeliveryStatus.IN_TRANSIT
    // Track courier info, pickup time
}

// Similar handlers for DROPPED_BY_COURIER and DELIVERED transitions
```

### 3. Integration Points

#### Event Flow

1. **Payment → Delivery Creation**
   - `PaymentMarkedPaidEvent` → creates `PackageDelivery` (EXISTS)

2. **Baker Drops Package**
   - `MarkDroppedByBakerCommand` → `PackageDroppedByBakerEvent` (EXISTS)
   - Event handled by product-delivery-service (read model) (EXISTS)
   - Event handled by **courier-service** (NEW) → creates anonymized offer

3. **Courier Accepts Offer**
   - REST call to courier-service
   - `AcceptDeliveryOfferCommand` → `DeliveryOfferAcceptedEvent` (NEW)
   - product-delivery-service updates tracking (NEW)

4. **Courier Gets Full Location**
   - REST call to courier-service with courier location
   - Business logic checks proximity
   - Returns full `PackageLocationInfo` with photo (NEW)

5. **Courier Picks Up Package**
   - REST call to either service
   - `MarkPickedUpByCourierCommand` → `PackagePickedUpByCourierEvent` (NEW)
   - Status: `IN_TRANSIT`

6. **Courier Drops at Destination**
   - REST call to product-delivery-service
   - `MarkDroppedByCourierCommand` → `PackageDroppedByCourierEvent` (NEW)
   - Status: `DROPPED_BY_COURIER`

7. **Recipient Confirms**
   - REST call to product-delivery-service
   - `MarkDeliveredCommand` → `PackageDeliveredEvent` (NEW)
   - Status: `DELIVERED`

#### Axon Framework Considerations

- All services connect to shared Axon Server (axon-server:8124)
- Events published by one service are automatically received by handlers in other services
- Use `@EventHandler` in courier-service to listen for events from product-delivery-service
- Commands are processed by aggregate repositories in their respective services
- Queries can cross services via QueryGateway but typically stay within service boundaries

### 4. Configuration and Deployment

#### Application Configuration Pattern

Each service needs `application-prod.yml` and `application-dev.yml`:

**application-prod.yml** (for Docker):
```yaml
spring:
  application:
    name: courier-service
  datasource:
    url: jdbc:postgresql://postgres-courier:5432/db
    username: admin
    password: admin
  jpa:
    hibernate:
      ddl-auto: update

server:
  port: 8080

axon:
  axonserver:
    servers: axon-server:8124

management:
  endpoints:
    web:
      exposure:
        include: [health, metrics, prometheus]

logging:
  loki:
    url: http://loki:3100
```

**application-dev.yml** (for local development):
```yaml
spring:
  application:
    name: courier-service
  datasource:
    url: jdbc:h2:mem:courierdb
  jpa:
    hibernate:
      ddl-auto: create-drop

server:
  port: 8087

axon:
  axonserver:
    servers: localhost:8124
```

#### Docker Compose Additions

Add to `docker-compose.yml`:

```yaml
postgres-courier:
  image: postgres:17
  ports:
    - 5439:5432
  environment:
    POSTGRES_USER: admin
    POSTGRES_PASSWORD: admin
    POSTGRES_DB: db

courier-service:
  image: kotlin-seminar/courier-service
  build:
    context: .
    dockerfile: courier-service/Dockerfile
  ports:
    - 8087:8080
  depends_on:
    - axon-server
    - postgres-courier
    - loki
    - prometheus
  environment:
    spring_profiles_active: prod
    LOKI_ADD: loki
    LOKI_PORT: 3100
  volumes:
    - ./spring/config:/config
```

#### Prometheus Configuration

Add to `prometheus.yml`:

```yaml
- job_name: 'courier-service'
  metrics_path: '/actuator/prometheus'
  static_configs:
    - targets: ['courier-service:8080']
      labels:
        service: 'courier-service'
```

#### Dockerfile

Copy from `product-delivery-service/Dockerfile` and update:
- Line 14: `COPY courier-service ./courier-service`
- Line 17: `RUN gradle :courier-service:clean :courier-service:bootJar --no-daemon`
- Line 20: `RUN ls -l courier-service/build/libs`
- Line 28: `COPY --from=build --chown=app-user:app-user /application/courier-service/build/libs/*.jar app.jar`
- Line 36: `CMD ["java", "-jar", "app.jar", "--spring.config.additional-location=/config/courier-service/"]`

### 5. Testing Strategy

#### Unit Tests
- Command handlers: verify state transitions
- Event handlers: verify read model updates
- Business logic: proximity checks, authorization

#### Integration Tests
- Event propagation from product-delivery-service to courier-service
- Full workflow: drop by baker → courier accepts → courier picks up

#### Manual Testing via Swagger
- Courier service: http://localhost:8087/swagger-ui.html
- Test anonymization: verify coordinates are rounded, no photo in anonymized DTO
- Test proximity check: try accessing full location from different courier positions

### 6. Implementation Order (Recommended)

1. **Setup courier-service module structure**
   - Copy directory structure
   - Create Application class
   - Configure build.gradle.kts, settings.gradle.kts
   - Add Docker, docker-compose, Prometheus configs

2. **Define shared contracts**
   - Events: DeliveryOfferAcceptedEvent, PackagePickedUpByCourierEvent
   - DTOs: AnonymizedPackageLocationInfoDTO, PackageLocationInfoDTO
   - Queries: AvailableDeliveriesQuery, PackageLocationQuery

3. **Implement courier-service read models**
   - Create JPA entities: AnonymizedPackageLocationInfo, PackageLocationInfo
   - Create repositories
   - Implement PackageDroppedByBakerEventHandler

4. **Implement courier acceptance flow**
   - Create CourierDeliveryAcceptance aggregate
   - Command: AcceptDeliveryOfferCommand
   - Event: DeliveryOfferAcceptedEvent
   - Controller endpoint: POST /courier/accept-delivery

5. **Implement location access control**
   - Controller endpoint: GET /courier/deliveries/{id}/location
   - Business logic: check courier acceptance + proximity
   - Return full location or 403

6. **Update product-delivery-service**
   - Add new commands: MarkPickedUpByCourierCommand, etc.
   - Add event sourcing handlers in PackageDelivery aggregate
   - Add event handlers for courier events
   - Add REST endpoints for pickup/drop/delivery

7. **End-to-end testing**
   - Build all: `./gradlew build`
   - Start stack: `docker compose up`
   - Test full workflow via Swagger UIs

### 7. Key Design Decisions To Make

The following design decisions need to be made during implementation:

1. **Proximity Threshold**
   - How close must courier be to see full location? (suggestion: 500m-1km)
   - Should threshold be configurable?

2. **Anonymization Precision**
   - How much to reduce coordinate precision? (suggestion: 2-3 decimal places = ~100m accuracy)
   - Should it be configurable per region?

3. **Concurrent Acceptance**
   - Can multiple couriers accept same delivery?
   - First-come-first-served, or assignment algorithm?

4. **Courier Authentication**
   - How is `courierId` provided? JWT token? Header?
   - Integration with existing auth system?

5. **Location Verification**
   - Trust courier's reported location?
   - Require GPS/mobile verification?

6. **State Transitions**
   - What happens if courier accepts but never picks up?
   - Timeout mechanism? Re-offer to other couriers?

7. **Read Model Consistency**
   - How to handle eventual consistency between services?
   - Retry logic for event processing failures?

### 8. API Documentation

Update `README.md` to add:

```markdown
### Courier Service
- **Swagger UI**: http://localhost:8087/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8087/api-docs
```

## Reference Files

### Key Existing Files to Study

1. **Service Structure Reference**: `product-delivery-service/` (newest service, good template)
2. **Aggregate Pattern**: `product-delivery-service/src/main/kotlin/org/pv293/kotlinseminar/productDeliveryService/application/aggregates/PackageDelivery.kt`
3. **Event Handler Pattern**: `product-delivery-service/src/main/kotlin/org/pv293/kotlinseminar/productDeliveryService/events/handlers/PackageDroppedByBakerEventHandler.kt`
4. **Controller Pattern**: `product-delivery-service/src/main/kotlin/org/pv293/kotlinseminar/productDeliveryService/controllers/PackageDeliveryController.kt`
5. **Build Config**: `product-delivery-service/build.gradle.kts`
6. **Docker Pattern**: `product-delivery-service/Dockerfile`

### Axon Framework Documentation

Primary docs: https://docs.axoniq.io/home/

Key concepts to understand:
- Command handling in aggregates
- Event sourcing
- Event handling in separate components
- Query handling with read models
- Sagas (if needed for complex workflows)

## Questions For Clarification

Before starting implementation, consider clarifying:

1. Should courier-service handle the actual pickup/drop commands, or should those stay in product-delivery-service?
2. What level of real-time tracking is needed for couriers?
3. Should there be notifications (email/SMS) at various stages?
4. Is there a payment/rating system for couriers?
5. Should the system support multiple drop locations (courier picks up, drops at multiple destinations)?

## Success Criteria

Implementation is complete when:

1. `courier-service` successfully listens to `PackageDroppedByBakerEvent`
2. Couriers can see list of available deliveries with anonymized locations
3. Couriers can accept delivery offers
4. Couriers receive full location info only when accepted and nearby
5. Full location includes photo URL, anonymized version does not
6. Package transitions through all states: CREATED → DROPPED_BY_BAKER → IN_TRANSIT → DROPPED_BY_COURIER → DELIVERED
7. All services run successfully in docker-compose
8. Swagger UI shows all endpoints for courier-service at port 8087
9. End-to-end workflow completes without errors

---

## Closing Notes

This handoff document provides a roadmap, not exact implementation details. The next developer should:
- Follow existing code patterns from other services
- Make pragmatic design decisions documented above
- Add appropriate logging for debugging
- Test incrementally, not big-bang
- Update this document if significant decisions change

Good luck! The existing services provide excellent reference implementations. When in doubt, copy the pattern from `product-delivery-service` and adapt for courier domain.
