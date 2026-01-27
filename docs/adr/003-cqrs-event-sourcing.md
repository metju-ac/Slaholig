# ADR-003: CQRS and Event Sourcing Pattern

**Status**: Accepted  
**Date**: 2025-12-24  
**Decision Makers**: Development Team  
**Technical Story**: Data persistence and state management pattern

---

## Context

In our event-driven microservices architecture, we need to decide how to persist and manage application state.
Traditional CRUD (Create-Read-Update-Delete) with relational databases has limitations for our use case:

### Requirements

1. **Audit Trail**: Complete history of all state changes for compliance and debugging
2. **Temporal Queries**: Ability to ask "what was the state at time T?"
3. **Event-Driven**: Already using events for inter-service communication (ADR-002)
4. **Read Optimization**: Different query patterns for different use cases (e.g., order history vs. real-time courier
   location)
5. **Scalability**: Separate read and write scaling
6. **Complex Business Logic**: Payment state machines, delivery workflows

### Seminar Context

Axon Framework (chosen in ADR-002) is built around CQRS and Event Sourcing. These patterns were taught in our seminars
as modern approaches to distributed system data management.

---

## Decision

We will implement **CQRS (Command Query Responsibility Segregation)** with **Event Sourcing** for all core aggregates in
each service.

### CQRS Pattern

**Command Side (Write Model)**:

- Commands express user intent (e.g., "Create Payment")
- Handled by aggregates
- Emit domain events
- No queries returned

**Query Side (Read Model)**:

- Queries retrieve data
- Handled by projections (separate read models)
- Optimized for specific query patterns
- Eventually consistent with write model

### Event Sourcing Pattern

- **State is derived from events**, not stored directly
- Aggregates store events in event store
- State is reconstructed by replaying events
- Event store is append-only (immutable)
- Every state change is an event

**Example**:

```
Payment Aggregate:
Event Store: [PaymentCreatedEvent, PaymentProcessingStartedEvent, PaymentSucceededEvent]
Current State: status = SUCCEEDED (derived by replaying all events)
```

---

## Rationale

### Why CQRS

**1. Separation of Concerns**:

```kotlin
// Write Model - focused on business rules
@Aggregate
class Payment {
    @CommandHandler
    fun handle(command: PayOrderCommand) {
        // Validate business rules
        if (status != PaymentStatus.CREATED) {
            throw IllegalStateException("Payment already processed")
        }
        // Emit event
        AggregateLifecycle.apply(PaymentSucceededEvent(...))
    }
}

// Read Model - optimized for queries
@Entity
class PaymentProjection {
    // Flat structure for fast queries
    val paymentId: UUID
    val orderId: UUID
    val amount: BigDecimal
    val status: String
    val createdAt: Instant
}
```

**2. Independent Scaling**:

- Write operations (commands) are less frequent than reads (queries)
- Can scale read model independently (add read replicas)
- Can use different databases for read/write if needed

**3. Query Optimization**:

- Read models can be denormalized for specific query patterns
- Example: Order history view doesn't need full aggregate complexity
- Can have multiple read models for same aggregate

**4. Technology Flexibility**:

- Write model uses event sourcing (Axon event store)
- Read model uses PostgreSQL (relational queries)
- Could use Elasticsearch for search, Redis for caching, etc.

### Why Event Sourcing

**1. Complete Audit Trail**:

```
Traditional: Payment status = SUCCEEDED
Event Sourced: 
  - PaymentCreatedEvent (2024-01-15 10:00:00)
  - PaymentProcessingStartedEvent (2024-01-15 10:00:05)
  - PaymentSucceededEvent (2024-01-15 10:00:10)
→ Full history of what happened and when
```

**2. Temporal Queries**:

- "What was payment status at 10:00:05?" → PROCESSING
- "How many payments were created yesterday?" → Replay events
- Useful for debugging production issues

**3. Event-Driven Architecture Alignment**:

- Already using events for inter-service communication (ADR-002)
- Events are first-class citizens
- Natural fit with event-driven patterns

**4. Bug Fixing**:

- Can fix bugs in business logic and replay events
- No data loss (events are immutable)
- Example: Fix calculation bug, replay events to get correct state

**5. Domain Modeling**:

- Events capture domain language ("Payment Succeeded" vs. "UPDATE payments SET status='SUCCEEDED'")
- Better communication with domain experts
- Self-documenting

**6. No Object-Relational Impedance Mismatch**:

- Don't need complex ORM mappings for aggregates
- Aggregate state is reconstructed from events
- Simpler aggregate code

### Trade-offs We Accept

**Storage Cost**:

- Event store grows indefinitely (every event stored)
- **Mitigation**: Events are small (JSON), storage is cheap, can archive old events

**Replay Performance**:

- Reconstructing aggregate from many events can be slow
- **Mitigation**: Snapshots (store periodic state snapshots), most aggregates have few events

**Eventual Consistency**:

- Read model is eventually consistent with write model
- **Mitigation**: Business domain tolerates this, usually milliseconds delay

**Complexity**:

- More complex than simple CRUD
- **Mitigation**: Axon handles most complexity, consistent patterns across services

**Event Schema Evolution**:

- Changing event schema requires careful versioning
- **Mitigation**: Upcasting in Axon, backward compatibility

---

## Alternatives Considered

### Alternative 1: Traditional CRUD with JPA/Hibernate

**Approach**: Store current state in relational database, update in place

**Pros**:

- Simple and familiar
- ACID transactions
- No eventual consistency
- Rich query support (SQL)

**Cons**:

- No audit trail (only current state)
- No temporal queries
- Object-relational impedance mismatch
- Tight coupling to database schema
- Harder to scale reads independently
- State transitions not explicit

**Example**:

```kotlin
@Entity
class Payment(
    @Id val paymentId: UUID,
    var status: PaymentStatus,
    var amount: BigDecimal
)

// Update in place (loses history)
payment.status = PaymentStatus.SUCCEEDED
paymentRepository.save(payment)
```

**Why rejected**:

- No audit trail (requirement)
- No temporal queries (requirement)
- Doesn't align with event-driven architecture (ADR-002)
- Not what Axon is designed for

### Alternative 2: CQRS without Event Sourcing

**Approach**: Separate read/write models, but store current state (not events)

**Pros**:

- Simpler than event sourcing
- Still get CQRS benefits (separation, independent scaling)
- Familiar persistence (JPA)

**Cons**:

- No audit trail (only current state)
- No temporal queries
- No event replay capability
- Half-measure (get some CQRS benefits but not all)

**Why rejected**:

- Axon is built for event sourcing
- Miss out on audit trail and temporal queries
- Already using events for communication, might as well persist them

### Alternative 3: Event Sourcing without CQRS

**Approach**: Use event sourcing but same model for reads and writes

**Pros**:

- Get audit trail and temporal queries
- Simpler than full CQRS

**Cons**:

- Inefficient queries (need to reconstruct aggregate)
- Can't optimize read model
- Can't scale reads independently
- All queries go through aggregate reconstruction

**Why rejected**:

- Query performance would suffer
- Miss out on CQRS optimization benefits
- Axon supports both, no reason not to use CQRS

### Alternative 4: Change Data Capture (CDC)

**Approach**: Use traditional database, capture changes as events (e.g., Debezium)

**Pros**:

- Keep traditional database
- Get events from database changes
- Familiar CRUD

**Cons**:

- Events are technical (database changes), not domain events
- Tight coupling to database schema
- Not first-class events
- Extra complexity (CDC infrastructure)
- Events derived from state, not state derived from events

**Why rejected**:

- Events should be domain-driven, not database-driven
- Backward (state-first, events-second) instead of forward (events-first, state-derived)
- More infrastructure for worse design

---

## Consequences

### Positive

- **Complete Audit Trail**: Every state change recorded as event
- **Temporal Queries**: Can query historical state
- **Event-Driven Native**: Events are first-class citizens
- **Bug Fixing**: Can fix logic and replay events
- **Scalability**: Separate read/write scaling
- **Query Optimization**: Multiple read models for different query patterns
- **Domain Modeling**: Events capture domain language

### Negative

- **Complexity**: More complex than simple CRUD
- **Storage**: Event store grows indefinitely
- **Eventual Consistency**: Read model not immediately consistent
- **Learning Curve**: Team needs to understand CQRS and Event Sourcing
- **Event Versioning**: Need careful schema evolution strategy

### Neutral

- **Snapshots**: May need snapshots for aggregates with many events
    - **Note**: Axon supports snapshots out of the box
- **Projections**: Need to maintain read model projections
    - **Note**: Straightforward with Axon's `@EventHandler`

---

## Implementation Details

### Write Model (Event Sourced Aggregates)

**Aggregate Structure**:

```kotlin
@Aggregate
class Payment {
    @AggregateIdentifier
    private lateinit var paymentId: UUID
    private lateinit var status: PaymentStatus
    private lateinit var amount: BigDecimal

    // Command handler - validates and emits events
    @CommandHandler
    fun handle(command: CreatePaymentCommand) {
        AggregateLifecycle.apply(
            PaymentCreatedEvent(
                paymentId = command.paymentId,
                orderId = command.orderId,
                amount = command.amount
            )
        )
    }

    // Event sourcing handler - rebuilds state
    @EventSourcingHandler
    fun on(event: PaymentCreatedEvent) {
        this.paymentId = event.paymentId
        this.status = PaymentStatus.CREATED
        this.amount = event.amount
    }
}
```

**Key Points**:

- `@Aggregate` marks it as event-sourced
- `@CommandHandler` validates business rules and emits events
- `@EventSourcingHandler` rebuilds state from events
- No direct state mutation (only via events)
- Axon stores events in event store

### Read Model (Projections)

**Projection Structure**:

```kotlin
@Entity
@Table(name = "payment_projection")
class PaymentProjection(
    @Id
    val paymentId: UUID,
    val orderId: UUID,
    val amount: BigDecimal,
    val status: String,
    val createdAt: Instant
)

// Event handler updates projection
@Component
class PaymentProjectionHandler(
    private val repository: PaymentProjectionRepository
) {
    @EventHandler
    fun on(event: PaymentCreatedEvent) {
        repository.save(PaymentProjection(
            paymentId = event.paymentId,
            orderId = event.orderId,
            amount = event.amount,
            status = "CREATED",
            createdAt = Instant.now()
        ))
    }

    @EventHandler
    fun on(event: PaymentSucceededEvent) {
        repository.findById(event.paymentId)?.let {
            it.status = "SUCCEEDED"
            repository.save(it)
        }
    }
}
```

**Key Points**:

- Regular JPA entity (not Axon aggregate)
- Updated by event handlers
- Optimized for specific queries
- Eventually consistent with write model

### Query Handlers

```kotlin
@Component
class PaymentQueryHandler(
    private val repository: PaymentProjectionRepository
) {
    @QueryHandler
    fun handle(query: PaymentQuery): PaymentDTO {
        val projection = repository.findById(query.paymentId)
            .orElseThrow { NotFoundException("Payment not found") }
        return PaymentDTO(
            paymentId = projection.paymentId,
            orderId = projection.orderId,
            amount = projection.amount,
            status = projection.status
        )
    }
}
```

### Snapshots

For aggregates with many events:

```kotlin
@Aggregate(snapshotTriggerDefinition = "mySnapshotTrigger")
class Payment {
    // After every 100 events, create snapshot
}
```

---

## Performance Considerations

**Write Performance**:

- Event append is fast (append-only log)
- No complex joins or updates
- Typically < 10ms per command

**Read Performance**:

- Query projection (not aggregate)
- Standard SQL queries
- Can add indexes to projections
- Typically < 5ms per query

**Aggregate Reconstruction**:

- Only on command handling (not queries)
- Most aggregates have < 100 events
- Snapshots for long-lived aggregates
- Typically < 50ms reconstruction

**Eventual Consistency Window**:

- Projection update is asynchronous
- Typically < 100ms delay
- Acceptable for our business domain

---

## Testing Strategy

**Aggregate Tests** (Given-When-Then):

```kotlin
@Test
fun `should create payment when command is valid`() {
    fixture.given(/* no events */)
        .`when`(
            CreatePaymentCommand(
                paymentId = paymentId,
                orderId = orderId,
                amount = BigDecimal("100.00")
            )
        )
        .expectEvents(
            PaymentCreatedEvent(
                paymentId = paymentId,
                orderId = orderId,
                amount = BigDecimal("100.00")
            )
        )
}
```

**Projection Tests**:

```kotlin
@Test
fun `should update projection when payment created`() {
    val event = PaymentCreatedEvent(...)
    handler.on(event)

    val projection = repository.findById(event.paymentId)
    assertThat(projection).isNotNull
    assertThat(projection.status).isEqualTo("CREATED")
}
```

---

## Compliance

This decision aligns with:

- Event-Driven Architecture (ADR-002)
- Microservices Architecture (ADR-001)
- Seminar teachings (Axon Framework, CQRS, Event Sourcing)
- Industry patterns (Martin Fowler, Greg Young)

---

## References

- [CQRS Pattern (Martin Fowler)](https://martinfowler.com/bliki/CQRS.html)
- [Event Sourcing (Martin Fowler)](https://martinfowler.com/eaaDev/EventSourcing.html)
- [Greg Young - CQRS Documents](https://cqrs.files.wordpress.com/2010/11/cqrs_documents.pdf)
- [Axon Framework - CQRS](https://docs.axoniq.io/reference-guide/architecture-overview/cqrs)
- [Axon Framework - Event Sourcing](https://docs.axoniq.io/reference-guide/architecture-overview/event-sourcing)
- [Microsoft - CQRS Pattern](https://learn.microsoft.com/en-us/azure/architecture/patterns/cqrs)
