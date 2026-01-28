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
- [ADR-005: Database per Service Pattern](docs/adr/005-database-per-service.md)
- [ADR-006: Spring Boot Framework](docs/adr/006-spring-boot-framework.md)

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
│   :8080       │          │    :8081      │          │    :8082      │──────────────┐
└───┬──────────┬┘          └───┬──────────┬┘          └──────────────┬┘              │ 
    │      ┌───▼────-┐         │      ┌───▼────-┐               ┌────▼────┐          │
    │      │ Postgres│         │      │ Postgres│               │ Postgres│          │
    │      └─────────┘         │      └─────────┘               └─────────┘          │
    │         ┌────────────────┘                                                     │
    │         │                                                                      │
    │         │            ┌───────────────┐                                         │
    │         │            │    Courier    │                                         │
    │         │            │    Service    │                                         │
    │         │            │    :8083      │                                         │
    │         │            └───────┬──────┬┘                                         │
    │         │                    │  ┌───▼────-┐                                    │
    │         │                    │  │ Postgres│                                    │
    │         │                    │  └─────────┘                                    │
    │         │                    │                                                 │
    │         │                    │                                                 │
    │         │    ┌───────────────┘                                                 │
    │   ┌─────▼────▼┐                                                                │
    └──>│   Axon    │◄───────── Event Bus & Command Bus                              │
        │   Server  │◄───────── Event Store                                          │
        │   :8024   │<───────────────────────────────────────────────────────────────┘
        └───────────┘
  
          
                ┌───────▼────┐      ┌──────────┐
                │ Prometheus │      │  Loki    │
                │   :9090    │      │  :3100   │
                └──────┬─────┘      └────┬─────┘
                       │                 │
                       └────────┬────────┘
                                │
                        ┌───────▼───────┐
                        │    Grafana    │
                        │     :3000     │
                        └───────────────┘
                        
 Prometheus scrapes metrics from:
   ├─ Product Selection Service (:8080 /actuator/prometheus)
   ├─ Payment Service           (:8081 /actuator/prometheus)
   ├─ Delivery Service          (:8082 /actuator/prometheus)
   ├─ Courier Service           (:8083 /actuator/prometheus)
   └─ Axon Server               (:8024)

 Grafana uses:
   ├─ Prometheus (metrics)
   └─ Loki (logs)
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

**Dependencies**: Share module (for all 4 services)

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

---

## Infrastructure

### Technology Stack

| Layer               | Technology     | Version | Purpose                        |
|---------------------|----------------|---------|--------------------------------|
| **Language**        | Kotlin         | 1.9+    | Primary development language   |
| **Framework**       | Spring Boot    | 3.x     | Application framework          |
| **Event Framework** | Axon Framework | 4.x     | CQRS and Event Sourcing        |
| **Build Tool**      | Gradle         | 8.x     | Build automation (Kotlin DSL)  |
| **JVM**             | OpenJDK        | 21      | Runtime environment            |
| **Database**        | PostgreSQL     | 17      | Per-service data persistence   |
| **Event Store**     | Axon Server    | Latest  | Event store and message router |
| **Monitoring**      | Prometheus     | Latest  | Metrics collection             |
| **Logging**         | Loki           | Latest  | Log aggregation                |
| **Visualization**   | Grafana        | Latest  | Dashboards and alerts          |
| **Container**       | Docker         | Latest  | Service containerization       |
| **Orchestration**   | Docker Compose | Latest  | Local development stack        |

### Deployment Architecture

**Local Development** (docker-compose.yml):

- All services containerized and orchestrated via Docker Compose
- Shared Docker network for inter-service communication
- Volume mounts for persistent data (Axon event store, PostgreSQL)
- Health checks and dependency ordering

**Service Configuration**:

- Environment-specific profiles (dev/prod)
- Spring Cloud Config for centralized configuration
- Externalized configuration via `/config` volume mount

**Database Architecture**:

- **Database per Service** pattern
- Each service has isolated PostgreSQL instance
- No cross-service database access
- Schema managed by Hibernate DDL (dev) or Flyway/Liquibase (prod)

### Build & Deployment

**Gradle Multi-Module Structure**:

```
Slaholig/
├── build.gradle.kts (root)
├── settings.gradle.kts
├── shared/
│   └── build.gradle.kts
├── product-selection-service/
│   ├── build.gradle.kts
│   └── Dockerfile
├── payment-service/
│   ├── build.gradle.kts
│   └── Dockerfile
├── product-delivery-service/
│   ├── build.gradle.kts
│   └── Dockerfile
└── courier-service/
    ├── build.gradle.kts
    └── Dockerfile
```

**Build Commands**:

- `./gradlew build` - Build all services
- `./gradlew :payment-service:build` - Build single service
- `./gradlew :payment-service:test` - Test single service
- `docker compose up` - Start full stack
- `docker compose up payment-service` - Start single service

**CI/CD** (Future):

- GitHub Actions for automated builds
- Docker image registry for versioned artifacts
- Kubernetes deployment for production
- Blue-green or canary deployments

### Scalability Considerations

**Horizontal Scaling**:

- Services are stateless (state in event store)
- Can run multiple instances behind load balancer
- Axon Server handles command routing and event distribution

**Vertical Scaling**:

- JVM tuning (heap size, GC configuration)
- Database connection pooling
- Async event processing

**Performance Optimization**:

- CQRS enables read replica scaling
- Event sourcing replays can be optimized via snapshots
- Eventual consistency reduces inter-service latency

---

## Development Guidelines

### Code Structure

All services follow consistent Clean Architecture pattern:

```
<service>/src/main/kotlin/org/pv293/kotlinseminar/<service>/
├── application/              # Core domain layer
│   ├── aggregates/          # Event-sourced aggregates
│   ├── commands/            # Command definitions
│   ├── queries/             # Query definitions
│   ├── dto/                 # Data Transfer Objects
│   ├── services/            # Domain services
│   └── projections/         # Read model projections
├── controllers/             # REST API adapters
├── events/                  # Service-local events
│   ├── impl/                # Event definitions
│   └── handlers/            # Event handlers and policies
├── infrastructure/          # Infrastructure configuration
│   └── AxonConfig.kt        # Axon Framework setup
├── repository/              # JPA repositories
└── <Service>Application.kt  # Spring Boot entry point
```

### Naming Conventions

- **Commands**: `<Action><Entity>Command` (e.g., `CreatePaymentCommand`)
- **Events**: `<Entity><PastTenseAction>Event` (e.g., `PaymentCreatedEvent`)
- **Queries**: `<Entity><What>Query` (e.g., `PaymentQuery`)
- **DTOs**: `<Entity>DTO` (e.g., `PaymentDTO`)
- **Handlers**: `<Event>Handler` or `<DomainConcept>Policy`


## Future Enhancements

1. **API Gateway**: Centralized entry point with routing, rate limiting, auth
2. **Service Discovery**: Eureka or Consul for dynamic service registration
3. **Circuit Breaker**: Resilience4j for fault tolerance
4. **Authentication**: OAuth2/JWT with Spring Security
5. **Snapshots**: Event sourcing snapshots for performance optimization
6. **Read Replicas**: Separate read-optimized projections for complex queries
7. **Kubernetes**: Production deployment with auto-scaling and self-healing
8. **Distributed Tracing**: OpenTelemetry / Jaeger for request tracing
9. **Automated Testing**: Comprehensive test suite with CI/CD integration

---

## References

- [Axon Framework Documentation](https://docs.axoniq.io/home/)
- [AWS ADR Guidance](https://docs.aws.amazon.com/prescriptive-guidance/latest/architectural-decision-records/adr-process.html)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Microservices Patterns](https://microservices.io/patterns/microservices.html)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
