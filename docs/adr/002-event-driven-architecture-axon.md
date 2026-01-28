# ADR-002: Event-Driven Architecture with Axon Framework

**Status**: Accepted  
**Date**: 2025-12-24
**Decision Makers**: Development Team  
**Technical Story**: Inter-service communication pattern for microservices

---

## Context

With our microservices architecture (see ADR-001), we need to decide how services will communicate with each other. Our
system has complex workflows that span multiple services:

- Order creation → Payment → Delivery → Fund release
- Package drop → Courier offers → Courier assignment
- Delivery completion → Payment settlement

### Requirements

1. **Loose Coupling**: Services should be independent and not directly call each other
2. **Asynchronous Processing**: Long-running workflows (payment processing, courier matching)
3. **Reliability**: Messages shouldn't be lost even if services are temporarily down
4. **Audit Trail**: Complete history of what happened and when
5. **Temporal Queries**: Ability to reconstruct past states
6. **Scalability**: Support high message throughput

### Seminar Context

We learned about Axon Framework in our university seminars for event-driven architecture with CQRS and Event Sourcing.
We found it to be a modern and well-designed framework.

---

## Decision

We will use **Event-Driven Architecture** with **Axon Framework** as the implementation platform.

### Architecture Pattern: Event-Driven Choreography

Services communicate exclusively via **domain events** published to Axon Server:

- No synchronous HTTP calls between services
- No shared database between services
- Services react to events from other services
- Workflows emerge from event chains (choreography, not orchestration)

### Technology: Axon Framework

**Axon Server** provides:

- Event Store (persistent event log)
- Event Bus (pub/sub for domain events)
- Command Bus (command routing)
- Query Bus (query distribution)

**Axon Framework** provides:

- CQRS infrastructure
- Event Sourcing support
- Aggregate lifecycle management
- Message routing and handler discovery

---

## Rationale

### Why Event-Driven Architecture

**1. Decoupling**:

```
Traditional Synchronous:
[Product Service] --HTTP--> [Payment Service] --HTTP--> [Delivery Service]
- Product Service needs to know Payment Service endpoint
- Failure in Payment blocks entire request
- Tight coupling

Event-Driven:
[Product Service] --OrderCreatedEvent--> [Event Bus]
                                             ↓
                              [Payment Service] subscribes
                                             ↓
                              --PaymentPaidEvent--> [Event Bus]
                                             ↓
                              [Delivery Service] subscribes
- Services don't know about each other
- Services can be down temporarily (events queued)
- Loose coupling
```

**2. Scalability**:

- Asynchronous processing allows services to process at their own pace
- Can add event handlers without modifying publishers
- Horizontal scaling of event consumers

**3. Audit Trail**:

- Every event is persisted in event store
- Complete history of system state changes
- Compliance and debugging support

**4. Extensibility**:

- New services can subscribe to existing events
- No changes needed to event publishers
- Example: Analytics service can subscribe to all events without modifying any service

**5. Resilience**:

- Temporary service failures don't lose messages
- Eventual consistency is acceptable for our business domain
- Automatic retry mechanisms

### Why Axon Framework

**1. Proven Technology**:

- Mature framework (10+ years)
- Used in production by many companies
- Active community and support

**2. CQRS + Event Sourcing Integration**:

- Built-in support for event sourcing aggregates
- Command/Query separation out of the box
- Event replay and temporal queries

**3. Developer Experience**:

- Annotation-based programming model (`@CommandHandler`, `@EventHandler`)
- Spring Boot integration
- Minimal boilerplate code

**4. Testing Support**:

- Test fixtures for aggregates
- Given-When-Then testing style
- Mock event infrastructure

**5. Educational Alignment**:

- Taught in our seminar courses
- Modern, idiomatic Kotlin support
- Industry-relevant skills

**Example Code**:

```kotlin
// Publishing side (Product Selection Service)
@Aggregate
class ShoppingCart {
    @CommandHandler
    fun handle(command: CreateOrderFromCartCommand) {
        // Business logic
        AggregateLifecycle.apply(
            OrderCreatedFromCartEvent(
                orderId = command.orderId,
                customerId = command.customerId,
                totalAmount = calculateTotal()
            )
        )
    }
}

// Consuming side (Payment Service)
@Component
class OrderCreatedEventHandler(
    private val commandGateway: CommandGateway
) {
    @EventHandler
    fun on(event: OrderCreatedFromCartEvent) {
        commandGateway.send<Any>(
            CreatePaymentCommand(
                paymentId = UUID.randomUUID(),
                orderId = event.orderId,
                amount = event.totalAmount
            )
        )
    }
}
```

### Trade-offs We Accept

**Eventual Consistency**:

- System state is not immediately consistent across services
- **Mitigation**: Our business domain tolerates this (e.g., few seconds delay for fund release is acceptable)

**Complexity**:

- More infrastructure components (Axon Server)
- Requires understanding of event-driven patterns
- **Mitigation**: Comprehensive documentation, consistent patterns across services

**Debugging Difficulty**:

- Harder to trace request flows across services
- **Mitigation**: Correlation IDs, centralized logging (Loki), Axon Server dashboard

**Message Ordering**:

- Events may arrive out of order
- **Mitigation**: Axon guarantees ordering per aggregate, which is sufficient for our use cases

---

## Alternatives Considered

### Alternative 1: Synchronous REST/HTTP

**Approach**: Services call each other directly via REST APIs

**Pros**:

- Immediate consistency
- Simple request-response model
- Easier debugging (single request trace)
- Familiar to most developers

**Cons**:

- Tight coupling (services need to know each other's endpoints)
- Cascading failures (if Payment is down, Order creation fails)
- Temporal coupling (all services must be up)
- Harder to scale (synchronous blocking)
- No audit trail
- Services directly depend on each other's availability

**Why rejected**: Creates tight coupling we want to avoid. Doesn't give us the resilience and scalability benefits of
async communication. Not what we learned in seminar.

### Alternative 2: Message Queue (RabbitMQ / Apache Kafka)

**Approach**: Services communicate via dedicated message broker

**RabbitMQ**:

- Message queue with routing
- AMQP protocol
- At-most-once or at-least-once delivery

**Apache Kafka**:

- Distributed log
- High throughput
- Event streaming

**Pros**:

- Proven message brokers
- Wide industry adoption
- Rich ecosystems
- Language-agnostic

**Cons**:

- Need to build CQRS infrastructure ourselves
- No built-in event sourcing support
- More boilerplate code
- Manual aggregate management
- No temporal query support
- More complex setup (Kafka cluster)

**Why rejected**: Axon gives us message broker + CQRS + Event Sourcing in one package. RabbitMQ/Kafka are lower-level
primitives that would require building Axon-like infrastructure ourselves. We'd be reinventing the wheel.

### Alternative 3: gRPC with Streaming

**Approach**: Services use gRPC for bi-directional streaming

**Pros**:

- Efficient binary protocol
- Streaming support
- Strong typing (Protocol Buffers)
- Good performance

**Cons**:

- Still synchronous request-response at core
- Services need to know about each other
- No event store / audit trail
- No event sourcing support
- More complex than REST but less decoupled than events

**Why rejected**: Doesn't give us event-driven benefits. Still tight coupling. Not what we learned in seminar.

### Alternative 4: AWS EventBridge / Azure Event Grid

**Approach**: Use cloud provider's event service

**Pros**:

- Fully managed
- High scalability
- Integration with cloud services

**Cons**:

- Vendor lock-in
- Can't run locally easily
- Requires cloud account
- No CQRS/Event Sourcing built-in
- Cost for development/testing

**Why rejected**: We want local-first development (Docker Compose). Don't want vendor lock-in. Axon Server is
self-hosted and works perfectly locally.

---

## Consequences

### Positive

- **Loose Coupling**: Services are independent and don't directly depend on each other
- **Scalability**: Asynchronous processing allows independent scaling
- **Audit Trail**: Complete event log in Axon Server event store
- **Temporal Queries**: Can reconstruct any past state by replaying events
- **Resilience**: Services can be down temporarily without losing messages
- **Extensibility**: Easy to add new services that react to existing events
- **Learning**: Practical experience with modern event-driven architecture

### Negative

- **Complexity**: More infrastructure components to manage
- **Debugging**: Harder to trace request flows across services
- **Eventual Consistency**: System state not immediately consistent
- **Learning Curve**: Team needs to understand event-driven patterns
- **Testing**: Need to test event flows, not just API endpoints

### Neutral

- **Event Versioning**: Need strategy for evolving events over time
    - **Mitigation**: Use upcasting in Axon, version shared events carefully
- **Event Schema**: Need to define and share event contracts
    - **Mitigation**: Shared module with cross-service event definitions

---

## Implementation Details

### Event Types

**Shared Events** (in `shared` module):

- Events that cross service boundaries
- Published by one service, consumed by others
- Examples: `OrderCreatedFromCartEvent`, `PaymentMarkedPaidEvent`

**Local Events** (in service modules):

- Events internal to a service
- Not consumed by other services
- Examples: `PaymentProcessingStartedEvent`, `DeliveryOfferCreatedEvent`

### Event Naming Convention

- Past tense: `<Entity><PastTenseAction>Event`
- Examples: `OrderCreatedEvent`, `PaymentSucceededEvent`, `CourierAssignedEvent`

### Event Handlers

**Event Handler** (simple reaction):

```kotlin
@EventHandler
fun on(event: OrderCreatedFromCartEvent) {
    // React to event
}
```

**Processing Group** (parallel processing):

```kotlin
@ProcessingGroup("payment-processor")
@EventHandler
fun on(event: OrderCreatedFromCartEvent) {
    // Isolated processing group
}
```


### Error Handling

- Failed event handlers logged with context
- Axon dead-letter queue for poison messages
- Retry policies per processing group
- Manual intervention UI in Axon Server dashboard

---

## Monitoring & Operations

**Axon Server Dashboard** (http://localhost:8024):

- View event store
- Monitor command/query/event rates
- Track processing groups
- Manage dead-letter queue

**Metrics** (Prometheus):

- Event processing rates
- Command dispatch latency
- Event handler errors
- Aggregate creation rates

**Logging** (Loki):

- Event publication logged with correlation IDs
- Event handler execution tracked
- Searchable via Grafana

---

## Compliance

This decision aligns with:

- Microservices architecture (ADR-001)
- Seminar teachings (Axon Framework)
- Industry patterns (Event-Driven Architecture, CQRS, Event Sourcing)
- Modern distributed systems design

---

## References

- [Axon Framework Documentation](https://docs.axoniq.io/home/)
- [Event-Driven Architecture (Martin Fowler)](https://martinfowler.com/articles/201701-event-driven.html)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [Event Sourcing (Martin Fowler)](https://martinfowler.com/eaaDev/EventSourcing.html)
- [Microservices.io - Event-Driven Architecture](https://microservices.io/patterns/data/event-driven-architecture.html)
