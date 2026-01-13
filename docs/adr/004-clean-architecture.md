# ADR-004: Clean Architecture for Service Internal Structure

**Status**: Accepted  
**Date**: 2025-12-24  
**Decision Makers**: Development Team  
**Technical Story**: Internal service architecture pattern

---

## Context

With our microservices architecture established (ADR-001), we need to decide how to structure code **within each service
**. The official assignment states: "The inner structure is our decision (Vertical Slices, Clean, ...)."

### Requirements

1. **Clear Separation of Concerns**: Business logic separate from infrastructure
2. **Testability**: Easy to test business logic without infrastructure
3. **Maintainability**: Easy to understand and modify
4. **Framework Independence**: Business logic not tied to Spring Boot
5. **Consistency**: Same pattern across all services
6. **DDD Compatibility**: Support Domain-Driven Design patterns (aggregates, entities)
7. **Axon Compatibility**: Work well with Axon Framework patterns

### Assignment Context

We have complete freedom to choose the internal structure:

- Vertical Slices (feature-based organization)
- Clean Architecture (dependency inversion)
- Layered Architecture (traditional layers)
- Hexagonal Architecture (ports and adapters)
- Or any other pattern

---

## Decision

We will use **Clean Architecture** (also known as Hexagonal Architecture / Ports and Adapters) for all microservices.

### Package Structure

```
<service>/src/main/kotlin/org/pv293/kotlinseminar/<service>/
├── application/              # Core Domain Layer
│   ├── aggregates/          # Event-sourced aggregates (Axon)
│   ├── commands/            # Command definitions
│   │   ├── impl/            # Concrete command classes
│   │   └── handlers/        # Optional command handlers (if not in aggregate)
│   ├── queries/             # Query definitions
│   │   ├── impl/            # Concrete query classes
│   │   └── handlers/        # Query handlers (read model)
│   ├── dto/                 # Data Transfer Objects
│   ├── services/            # Domain services (business logic)
│   └── projections/         # Read model projections (CQRS)
├── controllers/             # Adapter Layer (REST API)
├── events/                  # Service-local events
│   ├── impl/                # Event definitions
│   └── handlers/            # Event handlers (projections, policies)
├── infrastructure/          # Infrastructure Layer
│   └── AxonConfig.kt        # Framework configuration
├── repository/              # Adapter Layer (Persistence)
└── <Service>Application.kt  # Spring Boot entry point
```

### Dependency Rule

**Dependencies point inward**:

```
Controllers → Application (Domain)
Repository → Application (Domain)
Infrastructure → Application (Domain)
Application (Domain) → Nothing (no dependencies)
```

**Key Principle**: The `application/` package (core domain) has no dependencies on frameworks, databases, or REST APIs.
It only depends on `shared` module and standard library.

---

## Rationale

### Why Clean Architecture

**1. Separation of Concerns**:

```
Adapter (Controller):
    - Parses HTTP requests
    - Returns HTTP responses
    - Handles validation

Domain (Aggregate):
    - Business rules only
    - No HTTP knowledge
    - No database knowledge

Adapter (Repository):
    - Database queries
    - No business logic
```

**2. Testability**:

```kotlin
// Test domain without infrastructure
@Test
fun `payment should transition to SUCCEEDED when payment is valid`() {
    fixture.given(PaymentCreatedEvent(...))
    .`when`(PayOrderCommand(...))
    .expectEvents(PaymentSucceededEvent(...))
    // No Spring Boot, no database, no HTTP
}
```

**3. Framework Independence**:

- Business logic in `application/` doesn't import Spring
- Could swap Spring Boot for Micronaut without changing domain
- Domain logic is pure Kotlin + Axon annotations

**4. DDD Compatibility**:

- Aggregates are first-class citizens in `application/aggregates/`
- Domain services in `application/services/`
- Clear bounded context per service
- Ubiquitous language in code

**5. CQRS Compatibility**:

- Commands in `application/commands/`
- Queries in `application/queries/`
- Aggregates (write model) in `application/aggregates/`
- Projections (read model) in `application/projections/` or `repository/`

**6. Maintainability**:

- Easy to find things (consistent structure)
- Business logic isolated and easy to understand
- Infrastructure changes don't affect domain

**7. Consistency**:

- Same structure in all 4 services
- Developers can switch services easily
- Code reviews are easier

### Why Not Other Patterns

**Vertical Slices**:

- Feature-based organization (e.g., `/payment`, `/refund`, `/query`)
- Good for: Independent features, small services
- Why not: Our services have cohesive domain models (Payment aggregate), not independent features. CQRS naturally splits
  by command/query, not by feature.

**Layered Architecture**:

- Traditional layers: Presentation → Business → Persistence
- Good for: Simple CRUD applications
- Why not: Dependencies go both ways (presentation depends on business, business depends on persistence). Harder to
  test. Domain tied to database.

**Package by Feature** (simple):

- All code for a feature in one package
- Good for: Small applications, simple domains
- Why not: As services grow, need more structure. CQRS and Event Sourcing have specific needs (commands, queries,
  events, aggregates).

---

## Alternatives Considered

### Alternative 1: Layered Architecture (Traditional)

**Structure**:

```
├── controller/    (Presentation)
├── service/       (Business Logic)
├── repository/    (Persistence)
```

**Pros**:

- Simple and familiar
- Easy for beginners
- Widely used

**Cons**:

- Business logic depends on persistence (tight coupling)
- Hard to test without database
- Domain tied to framework
- Not suitable for DDD/CQRS

**Why rejected**: Dependencies go wrong direction (business layer depends on persistence layer). Doesn't work well with
event sourcing (no "service" layer in CQRS).

### Alternative 2: Vertical Slices (Feature-Based)

**Structure**:

```
├── createPayment/
│   ├── CreatePaymentCommand.kt
│   ├── CreatePaymentHandler.kt
│   ├── CreatePaymentController.kt
├── queryPayment/
│   ├── PaymentQuery.kt
│   ├── PaymentQueryHandler.kt
│   ├── PaymentController.kt
```

**Pros**:

- High cohesion per feature
- Easy to delete features
- Good for independent features

**Cons**:

- Doesn't fit DDD (aggregates span features)
- Doesn't fit Event Sourcing (aggregate handles multiple commands)
- Harder to find cross-cutting concerns
- More duplication across slices

**Why rejected**: Our services have cohesive aggregates (Payment, PackageDelivery) that handle multiple commands.
Vertical slices would split aggregate across packages. CQRS naturally separates command/query, not features.

### Alternative 3: Hexagonal Architecture (Explicit Ports/Adapters)

**Structure**:

```
├── domain/         (Core business logic)
├── ports/          (Interfaces)
│   ├── in/         (Incoming ports - use cases)
│   ├── out/        (Outgoing ports - repositories)
├── adapters/       (Implementations)
│   ├── in/         (REST controllers)
│   ├── out/        (Database repositories)
```

**Pros**:

- Very explicit about ports and adapters
- Clear dependency boundaries
- Textbook hexagonal architecture

**Cons**:

- More verbose (explicit port interfaces)
- Axon already provides ports (CommandGateway, EventBus)
- Don't need extra interfaces when Axon provides them
- More boilerplate

**Why rejected**: Clean Architecture is essentially hexagonal architecture with less boilerplate. Axon already provides
the "ports" (CommandGateway, QueryGateway, EventBus). Don't need explicit port interfaces on top.

### Alternative 4: Package by Component

**Structure**:

```
├── payment/        (Payment component)
│   ├── Payment.kt
│   ├── PaymentController.kt
│   ├── PaymentRepository.kt
│   ├── PaymentService.kt
```

**Pros**:

- All related code together
- Easy to see component dependencies
- Works for monoliths

**Cons**:

- Not suitable for CQRS (commands/queries are separate)
- Not suitable for Event Sourcing (aggregates/projections are separate)
- Mix of concerns in one package

**Why rejected**: Doesn't fit CQRS paradigm. We have separate write model (aggregates) and read model (projections).
Also, controllers are adapters, not domain.

---

## Consequences

### Positive

- **Testability**: Can test business logic without Spring Boot or database
- **Maintainability**: Clear separation of concerns, easy to understand
- **Framework Independence**: Domain logic not tied to Spring
- **DDD Support**: Natural place for aggregates, services, domain events
- **CQRS Support**: Clear separation of commands, queries, aggregates, projections
- **Consistency**: Same structure in all services
- **Learning**: Industry-standard pattern, transferable skills

### Negative

- **Initial Complexity**: More packages and structure than simple layered architecture
- **Learning Curve**: Developers need to understand Clean Architecture principles
- **Boilerplate**: More files (commands, events, handlers) than CRUD

### Neutral

- **Axon Specifics**: Axon aggregates blur line between domain and infrastructure
    - **Note**: We treat aggregates as domain (they contain business logic)
- **JPA Entities**: Projection entities are both domain (what data) and infrastructure (how stored)
    - **Note**: We treat projections as infrastructure (they're just caches of event-sourced state)

---

## Implementation Guidelines

### Application Layer (Domain Core)

**Aggregates** (`application/aggregates/`):

- Contain business logic
- Handle commands
- Emit events
- Reconstruct state from events
- No Spring dependencies (only Axon annotations)

```kotlin
@Aggregate
class Payment {
    @AggregateIdentifier
    private lateinit var paymentId: UUID

    @CommandHandler
    fun handle(command: PayOrderCommand) {
        // Business rules here
        if (status != PaymentStatus.CREATED) {
            throw IllegalStateException("Payment already processed")
        }
        AggregateLifecycle.apply(PaymentSucceededEvent(...))
    }
}
```

**Commands** (`application/commands/impl/`):

- Express user intent
- Immutable data classes
- No business logic

```kotlin
data class CreatePaymentCommand(
    @TargetAggregateIdentifier
    val paymentId: UUID,
    val orderId: UUID,
    val amount: BigDecimal
)
```

**Domain Services** (`application/services/`):

- Business logic that doesn't fit in aggregates
- Stateless
- Called by aggregates or event handlers

```kotlin
class PaymentGatewayService {
    fun processPayment(amount: BigDecimal): PaymentResult {
        // Domain logic for payment processing
    }
}
```

### Adapter Layer

**Controllers** (`controllers/`):

- REST API endpoints
- Parse HTTP requests
- Call Axon gateways (CommandGateway, QueryGateway)
- Return DTOs

```kotlin
@RestController
@RequestMapping("/api/payment")
class PaymentController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway
) {
    @PostMapping("/initiate/{orderId}")
    fun initiatePayment(@PathVariable orderId: UUID): PaymentDTO {
        val paymentId = UUID.randomUUID()
        commandGateway.sendAndWait<Any>(CreatePaymentCommand(paymentId, orderId, ...))
        return queryGateway.query(PaymentQuery(paymentId), PaymentDTO::class.java).join()
    }
}
```

**Repositories** (`repository/`):

- JPA repositories for projections
- No business logic
- Just data access

```kotlin
@Repository
interface PaymentProjectionRepository : JpaRepository<PaymentProjection, UUID>
```

### Infrastructure Layer

**Configuration** (`infrastructure/`):

- Axon configuration
- Spring configuration
- Framework setup

```kotlin
@Configuration
class AxonConfig {
    @Bean
    fun eventProcessingConfigurer(): EventProcessingConfigurer {
        // Axon setup
    }
}
```

---

## Validation

Each service follows this structure:

✅ **Product Selection Service**:

- `application/aggregates/` - BakedGood, ShoppingCart, ChosenLocation, Order
- `controllers/` - BakedGoodsController, ShoppingCartController, OrderController
- `events/handlers/` - OrderProjectionEventHandler

✅ **Payment Service**:

- `application/aggregates/` - Payment
- `controllers/` - PaymentController
- `events/handlers/` - OrderCreatedEventHandler, PayrollPolicy
- `application/services/` - CryptoPaymentGatewayService, PayrollService

✅ **Product Delivery Service**:

- `application/aggregates/` - PackageDelivery
- `controllers/` - PackageDeliveryController
- `events/handlers/` - PaymentMarkedPaidEventHandler, DeliveryOfferAcceptedEventHandler
- `application/services/` - EmailNotificationService

✅ **Courier Service**:

- `application/aggregates/` - CourierQueue, AvailableDeliveryOffer, PackageLocationInfo
- `controllers/` - CourierController, DeliveryOfferController
- `events/handlers/` - CourierNeededPolicy, OfferAcceptedPolicy
- `application/services/` - CourierNotificationService

All services follow the same structure consistently.

---

## Future Evolution

**Potential Enhancements**:

1. **Explicit Port Interfaces**: If we add multiple adapters (e.g., gRPC + REST), create explicit port interfaces
2. **Use Cases**: Extract command handlers into explicit use case classes (separate from aggregates)
3. **Domain Events**: Move more events to domain layer (currently in `events/`)
4. **Value Objects**: Extract more value objects (Amount, PaymentStatus as first-class types)

---

## Compliance

This decision aligns with:

- Assignment requirements (we choose internal structure)
- Microservices architecture (ADR-001)
- Event-Driven Architecture (ADR-002)
- CQRS and Event Sourcing (ADR-003)
- Clean Code principles
- Domain-Driven Design

---

## References

- [Clean Architecture (Robert C. Martin)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Hexagonal Architecture (Alistair Cockburn)](https://alistair.cockburn.us/hexagonal-architecture/)
- [DDD and Hexagonal Architecture](https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/)
- [Ports and Adapters Pattern](https://softwarecampament.wordpress.com/portsadapters/)
- [Clean Architecture with Spring Boot](https://medium.com/swlh/clean-architecture-java-spring-fea51e26e00)
