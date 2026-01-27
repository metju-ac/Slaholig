# Architecture Documentation

## Table of Contents

1. [System Overview](#system-overview)
2. [Architectural Decisions](#architectural-decisions)
3. [Architecture Patterns](#architecture-patterns)
4. [Service Details](#service-details)
5. [Cross-Cutting Concerns](#cross-cutting-concerns)
6. [Infrastructure](#infrastructure)

---

## System Overview

**Slaholig** is a comprehensive bakery goods delivery platform that enables customers to order baked goods and have them
delivered via an autonomous courier network. The system handles the complete order-to-delivery lifecycle including
product selection, payment processing, baker-to-courier handoff, and final customer delivery.

### Business Domain

The platform implements a "dead-drop" delivery model:

1. Customers browse and purchase baked goods
2. Payment is processed via cryptocurrency
3. Bakers drop packages at designated locations
4. System finds nearby available couriers
5. Couriers pick up and deliver to customer locations
6. System validates delivery proximity (within 100m) via geolocation
7. Funds are released to bakers after successful delivery

### Core Services

| Service               | Port | Responsibility                                 | Database Port |
|-----------------------|------|------------------------------------------------|---------------|
| **Product Selection** | 8080 | Product catalog, shopping cart, order creation | 5436          |
| **Payment**           | 8081 | Payment processing, fund management            | 5437          |
| **Product Delivery**  | 8082 | Delivery lifecycle management                  | 5438          |
| **Courier**           | 8083 | Courier availability, delivery offers          | 5439          |
| **Shared**            | N/A  | Cross-service contracts and utilities          | N/A           |

### Key Quality Attributes

- **Scalability**: Microservices can be scaled independently based on load
- **Resilience**: Event-driven architecture enables loose coupling and fault tolerance
- **Consistency**: Event sourcing provides complete audit trail and temporal queries
- **Extensibility**: New services can be added by subscribing to existing events
- **Observability**: Centralized monitoring via Grafana, Prometheus, and Loki

---

## Architectural Decisions

All architectural decisions are documented using Architecture Decision Records (ADRs) in the `/docs/adr/` directory:

- [ADR-001: Microservices Architecture](./docs/adr/001-microservices-architecture.md)
- [ADR-002: Event-Driven Architecture with Axon Framework](./docs/adr/002-event-driven-architecture-axon.md)
- [ADR-003: CQRS and Event Sourcing Pattern](./docs/adr/003-cqrs-event-sourcing.md)
- [ADR-004: Clean Architecture for Service Internal Structure](./docs/adr/004-clean-architecture.md)
- [ADR-005: Kotlin as Primary Language](./docs/adr/005-kotlin-language.md)
- [ADR-006: Database per Service Pattern](./docs/adr/006-database-per-service.md)
- [ADR-007: Saga Pattern for Distributed Transactions](./docs/adr/007-saga-pattern.md)
- [ADR-008: Spring Boot Framework](./docs/adr/008-spring-boot-framework.md)

---

## Architecture Patterns

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         API Gateway / Load Balancer              │
│                          (Future - Currently Direct)             │
└─────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────────────────────┼───────────────────────────┐
        │                           │                           │
        ▼                           ▼                           ▼
┌───────────────┐          ┌───────────────┐          ┌───────────────┐
│   Product     │          │    Payment    │          │   Delivery    │
│   Selection   │          │    Service    │          │    Service    │
│   Service     │          │               │          │               │
│   :8080       │          │    :8081      │          │    :8082      │
└───────┬───────┘          └───────┬───────┘          └───────┬───────┘
        │                          │                          │
        │                          │                          │
        │     ┌────────────────────┼──────────────────────────┘
        │     │                    │
        │     │            ┌───────▼───────┐
        │     │            │    Courier    │
        │     │            │    Service    │
        │     │            │    :8083      │
        │     │            └───────┬───────┘
        │     │                    │
        └─────┼────────────────────┘
              │
        ┌─────▼─────┐
        │   Axon    │◄───────── Event Bus & Command Bus
        │   Server  │◄───────── Event Store
        │   :8024   │
        └───────────┘
              │
    ┌─────────┼─────────┐
    │                   │
┌───▼────┐      ┌───────▼────┐      ┌──────────┐
│ Postgres│     │ Prometheus │      │  Loki    │
│ (x4)    │     │   :9090    │      │  :3100   │
└─────────┘     └──────┬─────┘      └────┬─────┘
                       │                  │
                       └────────┬─────────┘
                                │
                        ┌───────▼───────┐
                        │    Grafana    │
                        │     :3000     │
                        └───────────────┘
```

### Event-Driven Choreography

Services communicate exclusively via domain events published to Axon Server. There is no direct service-to-service
communication via REST/HTTP between microservices.

**Example Event Flow: Order Creation to Fund Release**

```
[Customer] → [Product Selection API] → CreateOrderFromCartCommand
                                             │
                                             ▼
                                  OrderCreatedFromCartEvent
                                             │
                    ┌────────────────────────┴────────────────────────┐
                    ▼                                                 ▼
          [Payment Service]                              [Other Services]
       OrderCreatedEventHandler                          (if interested)
                    │
                    ▼
          CreatePaymentCommand → Payment Aggregate
                    │
                    ▼
          PaymentCreatedEvent
                    │
        (Customer pays via API)
                    │
                    ▼
          PayOrderCommand → Payment Aggregate
                    │
                    ├─→ PaymentProcessingStartedEvent
                    ├─→ PaymentSucceededEvent
                    └─→ PaymentMarkedPaidEvent
                                 │
                ┌────────────────┴────────────────┐
                ▼                                 ▼
    [Delivery Service]                   [Other Services]
 PaymentMarkedPaidEventHandler
                │
                ├─→ Send Email to Baker
                └─→ CreatePackageDeliveryCommand
                                 │
                                 ▼
                    PackageDeliveryCreatedEvent
                                 │
                    (Baker drops package)
                                 │
                                 ▼
                    PackageDroppedByBakerEvent
                                 │
                ┌────────────────┴────────────────┐
                ▼                                 ▼
        [Courier Service]                [Delivery Service]
     CourierNeededPolicy                (stores location)
                │
                ├─→ Find nearby couriers (< 5km)
                └─→ CreateDeliveryOfferCommand (for each)
                                 │
                                 ▼
                    DeliveryOfferCreatedEvent
                                 │
                    (Courier accepts offer)
                                 │
                                 ▼
                    DeliveryOfferAcceptedEvent
                                 │
                                 ▼
                    [Delivery Service]
              DeliveryOfferAcceptedEventHandler
                                 │
                                 ▼
                    AssignCourierCommand
                                 │
                    (Courier delivers)
                                 │
                                 ▼
                    PackageRetrievedEvent
                                 │
                                 ▼
                    [Payment Service]
                     PayrollPolicy
                                 │
                                 └─→ ReleaseFundsCommand
                                            │
                                            ▼
                                   FundsReleasedEvent
```

### CQRS Pattern

Each service implements Command Query Responsibility Segregation:

**Write Model (Command Side)**:

- Event-sourced aggregates with `@Aggregate` annotation
- Aggregates handle commands via `@CommandHandler`
- State changes emit domain events via `AggregateLifecycle.apply()`
- Events rebuild state via `@EventSourcingHandler`
- Single source of truth stored in Axon event store

**Read Model (Query Side)**:

- Separate projection entities (JPA entities)
- Event handlers update projections when domain events occur
- Query handlers serve read requests via `@QueryHandler`
- Optimized for specific query patterns
- Eventually consistent with write model

**Example: Payment Service**

```kotlin
// Write Model
@Aggregate
class Payment {
    @CommandHandler
    fun handle(command: CreatePaymentCommand) {
        AggregateLifecycle.apply(PaymentCreatedEvent(...))
    }

    @EventSourcingHandler
    fun on(event: PaymentCreatedEvent) {
        // Rebuild state from event
        this.paymentId = event.paymentId
        this.status = PaymentStatus.CREATED
    }
}

// Read Model (if needed for complex queries)
@Entity
class PaymentProjection {
    // Optimized for queries
}

@EventHandler
fun on(event: PaymentCreatedEvent) {
    // Update projection
    paymentRepository.save(PaymentProjection(...))
}
```

---

## Service Details

### Product Selection Service

**Domain Model**:

- `BakedGood`: Product catalog with pricing, reviews, and stock
- `ShoppingCart`: Customer cart with items and quantities
- `ChosenLocation`: Delivery address for customer
- `Order`: Order projection (read model)

**Key Aggregates**:

- `BakedGood` (`application/aggregates/BakedGood.kt`)
- `ShoppingCart` (`application/aggregates/ShoppingCart.kt`)
- `ChosenLocation` (`application/aggregates/ChosenLocation.kt`)

**Published Events** (shared):

- `OrderCreatedFromCartEvent` - Triggers payment and downstream processes

**API Endpoints**:

- `/api/baked-goods` - Product catalog CRUD
- `/api/shopping-cart` - Cart management
- `/api/cart/checkout` - Order creation
- `/api/orders` - Order queries
- `/api/location` - Delivery location management
- `/api/baked-goods/{id}/reviews` - Product reviews

**Dependencies**: None (entry point service)

---

### Payment Service

**Domain Model**:

- `Payment`: Payment lifecycle with status transitions
    - CREATED → PROCESSING → PAID → RELEASED

**Key Aggregate**:

- `Payment` (`application/aggregates/Payment.kt`)

**Event Handlers**:

- `OrderCreatedEventHandler` - Creates payment when order is placed
- `PayrollPolicy` - Releases funds when package is retrieved

**Published Events** (shared):

- `PaymentMarkedPaidEvent` - Triggers delivery creation

**Published Events** (local):

- `PaymentCreatedEvent`
- `PaymentProcessingStartedEvent`
- `PaymentSucceededEvent`
- `FundsReleasedEvent`

**External Integrations**:

- `CryptoPaymentGatewayService` - Mock cryptocurrency payment gateway
- `PayrollService` - Mock fund release to merchants

**API Endpoints**:

- `POST /api/payment/initiate/{orderId}` - Start payment for order
- `GET /api/payment/{paymentId}` - Query payment status

**Dependencies**: Product Selection Service (via `OrderCreatedFromCartEvent`)

---

### Product Delivery Service

**Domain Model**:

- `PackageDelivery`: Delivery lifecycle with status transitions
    - CREATED → DROPPED_BY_BAKER → IN_TRANSIT → DROPPED_BY_COURIER → RETRIEVED

**Key Aggregate**:

- `PackageDelivery` (`application/aggregates/PackageDelivery.kt`)

**Event Handlers**:

- `PaymentMarkedPaidEventHandler` - Creates delivery when payment succeeds
- `DeliveryOfferAcceptedEventHandler` - Assigns courier when offer accepted
- `PackageDroppedByCourierEventHandler` - Handles package drop

**Published Events** (shared):

- `PackageDroppedByBakerEvent` - Triggers courier offers
- `PackageRetrievedEvent` - Triggers fund release

**Published Events** (local):

- `PackageDeliveryCreatedEvent`
- `CourierAssignedEvent`
- `PackagePickedUpByCourierEvent`
- `PackageDroppedByCourierEvent`

**External Integrations**:

- `EmailNotificationService` - Mock email to bakers

**Geolocation Validation**:

- Uses `GeoDistanceCalculator` (shared) to validate courier drop location
- Ensures courier drops within 100m of customer location

**API Endpoints**:

- `POST /api/deliveries/{deliveryId}/mark-dropped-by-baker` - Baker drops package
- `POST /api/deliveries/{deliveryId}/mark-picked-up-by-courier` - Courier picks up
- `POST /api/deliveries/{deliveryId}/mark-dropped-by-courier` - Courier drops
- `POST /api/deliveries/{deliveryId}/retrieve` - Customer retrieves
- `GET /api/deliveries/{deliveryId}` - Query delivery status

**Dependencies**:

- Payment Service (via `PaymentMarkedPaidEvent`)
- Courier Service (via `DeliveryOfferAcceptedEvent`)

---

### Courier Service

**Domain Model**:

- `CourierQueue`: Courier availability and location tracking
- `AvailableDeliveryOffer`: Delivery offers to nearby couriers
    - PENDING → ACCEPTED/CANCELLED
- `PackageLocationInfo`: Full precision package location
- `AnonymizedPackageLocationInfo`: Privacy-reduced location (2 decimal places)

**Key Aggregates**:

- `CourierQueue` (`application/aggregates/CourierQueue.kt`)
- `AvailableDeliveryOffer` (`application/aggregates/AvailableDeliveryOffer.kt`)
- `PackageLocationInfo` (`application/aggregates/PackageLocationInfo.kt`)
- `AnonymizedPackageLocationInfo` (`application/aggregates/AnonymizedPackageLocationInfo.kt`)

**Event Handlers / Policies**:

- `CourierNeededPolicy` - Creates offers for nearby couriers (< 5km)
- `PackageDroppedByBakerEventHandler` - Stores location info
- `OfferAcceptedPolicy` - Cancels other offers when one is accepted

**Published Events** (shared):

- `CourierMarkedAvailableEvent`
- `CourierMarkedUnavailableEvent`
- `CourierLocationUpdatedEvent`
- `DeliveryOfferAcceptedEvent` - Triggers courier assignment

**Published Events** (local):

- `DeliveryOfferCreatedEvent`
- `DeliveryOfferCancelledEvent`

**External Integrations**:

- `CourierNotificationService` - Mock notifications to couriers

**Geolocation Features**:

- Haversine formula for distance calculation
- Finds couriers within 5km of package drop location
- Stores both full and anonymized locations

**API Endpoints**:

- `POST /api/courier/{courierId}/mark-available` - Mark courier available
- `POST /api/courier/{courierId}/mark-unavailable` - Mark courier unavailable
- `POST /api/courier/{courierId}/update-location` - Update courier location
- `GET /api/courier/available` - Query available couriers
- `GET /api/delivery-offer/{courierId}/available` - Get offers for courier
- `POST /api/delivery-offer/{offerId}/accept` - Accept delivery offer
- `GET /api/delivery-offer/package-location/{deliveryId}` - Get package location

**Dependencies**: Product Delivery Service (via `PackageDroppedByBakerEvent`)

---

### Shared Module

**Purpose**: Cross-service contracts and common utilities

**Contents**:

1. **Shared Events** (contracts between services):
    - `OrderCreatedFromCartEvent` - From Product Selection
    - `PaymentMarkedPaidEvent` - From Payment
    - `PackageDroppedByBakerEvent` - From Delivery
    - `PackageRetrievedEvent` - From Delivery
    - `DeliveryOfferAcceptedEvent` - From Courier
    - `CourierMarkedAvailableEvent` - From Courier
    - `CourierMarkedUnavailableEvent` - From Courier
    - `CourierLocationUpdatedEvent` - From Courier

2. **Shared Queries**:
    - `PackageLocationQuery` - Query package location
    - `AvailableDeliveryOffersQuery` - Query offers for courier
    - `AvailableCouriersQuery` - Query available couriers
    - `PackageDeliveryQuery` - Query delivery status

3. **Shared DTOs**:
    - `PackageDeliveryDTO`
    - `PackageLocationDTO`
    - `AvailableDeliveryOfferDTO`
    - `AvailableCourierDTO`

4. **Common Exceptions**:
    - `NotFoundException` - Used across all services

5. **Utilities**:
    - `GeoDistanceCalculator` - Haversine formula for geolocation calculations

**Design Principle**:

- Only events that cross service boundaries are placed in `shared`
- Local events remain in service modules
- Keeps coupling minimal and explicit
- Changes to shared contracts require careful coordination

---

## Cross-Cutting Concerns

### Transaction Management

**Saga Pattern** is used for distributed transactions across services. Implemented via Axon's event-driven policies:

**Example: Order-to-Delivery Saga**

1. **Happy Path**:
    - Order created → Payment created → Payment paid → Delivery created → Courier assigned → Package delivered → Funds
      released

2. **Compensating Transactions**:
    - Payment fails → Order marked as failed (future enhancement)
    - No courier accepts → Offer timeout and retry logic (future enhancement)
    - Customer doesn't retrieve → Automatic return process (future enhancement)

### Error Handling

**HTTP Layer**:

- `ResponseStatusException` for request validation errors
- `NotFoundException` (shared) mapped to 404 at API boundary
- Consistent error response format across services

**Event Processing**:

- Failed event handlers logged with context
- Dead-letter queue for poison messages (Axon configuration)
- Retry policies configured per processing group

**Aggregate Validation**:

- Business rule violations throw exceptions before event emission
- Aggregate state validated in command handlers
- No invalid state persisted to event store

### Logging

**Strategy**:

- SLF4J with Logback
- Structured logging with key fields (IDs, status, amounts)
- Aggregated via Loki and visualized in Grafana

**Log Levels**:

- `INFO`: Normal workflow (command received, event published)
- `WARN`: Recoverable issues (retry, timeout)
- `ERROR`: Failures requiring intervention (payment declined, validation failure)

**Example**:

```kotlin
logger.info("Payment processing started for orderId: ${command.orderId}, amount: ${command.amount}")
logger.error("Payment failed for paymentId: ${paymentId}, reason: ${exception.message}", exception)
```

### Monitoring & Observability

**Metrics** (Prometheus):

- JVM metrics (heap, threads, GC)
- Spring Boot Actuator metrics
- Axon Framework metrics (command/query/event rates)
- Custom business metrics (orders created, payments processed)

**Logs** (Loki):

- Centralized log aggregation
- Correlation IDs for tracing across services
- Searchable and queryable via Grafana

**Dashboards** (Grafana):

- Pre-configured "Spring Microservices" dashboard
- Service health overview
- Event processing rates
- Database connection pools
- HTTP request metrics

**Access**:

- Grafana: http://localhost:3000/d/spring-microservices (admin/admin)
- Prometheus: http://localhost:9090
- Axon Server: http://localhost:8024

### Security

**Current State** (Development):

- No authentication/authorization implemented
- Database credentials in docker-compose (dev only)
- API endpoints publicly accessible

**Future Enhancements**:

- Spring Security with OAuth2/JWT
- API Gateway with centralized auth
- Service-to-service authentication via Axon
- Database credential management via secrets
