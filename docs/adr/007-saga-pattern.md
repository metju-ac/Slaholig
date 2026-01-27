# ADR-007: Saga Pattern for Distributed Transactions

**Status**: Accepted  
**Date**: 2025-12-31
**Decision Makers**: Development Team  
**Technical Story**: Managing distributed transactions across microservices

---

## Context

In a microservices architecture with the Database per Service pattern (ADR-006), each service manages its own database
transactions. However, many business processes span multiple services and require coordinated updates across multiple
databases.

Traditional distributed transactions (2PC - Two-Phase Commit) are not suitable for microservices because:

- They create tight coupling between services
- They reduce availability (all services must be up for transaction to complete)
- They create contention and reduce scalability
- They don't align with event-driven architecture

### Key Requirements

1. **Cross-Service Workflows**: Order → Payment → Delivery → Courier Assignment → Fund Release
2. **Failure Compensation**: If payment fails, the order should be cancelled
3. **Eventual Consistency**: Data consistency across services without distributed transactions
4. **Loose Coupling**: Services should remain independent

### Business Workflows

The Slaholig platform has several cross-service workflows:

1. **Order-to-Delivery Flow**: Order → Payment → Delivery → Courier Assignment → Fund Release
2. **Payment Failure Compensation**: If payment fails, the order should be cancelled
3. **Courier Assignment**: If no courier accepts, the system should retry or notify staff

---

## Decision

We will use the **Saga Pattern** implemented via **event-driven choreography** using Axon Framework's event handlers and
policies.

A Saga coordinates a sequence of local transactions across services, with compensating transactions to roll back changes
if any step fails.

### Implementation Approach

We use **choreography-based sagas** where:

- Each service publishes domain events after completing its local transaction
- Other services listen to these events and perform their local transactions
- Policies encapsulate saga logic and compensation
- No central orchestrator - services coordinate through events

---

## Rationale

### Why Saga Pattern

Sagas provide a way to maintain data consistency across services without distributed transactions:

**1. Event-Driven Alignment**:

- Natural fit with our event-driven architecture (ADR-002)
- Uses existing Axon infrastructure
- Events already flow between services

**2. Loose Coupling**:

- Services don't need to know about each other
- Only care about events they subscribe to
- Can add/modify services without changing others

**3. Resilience**:

- Services can be down temporarily
- Events are queued and processed when service recovers
- No global locks or blocking

### Choreography vs. Orchestration

| Aspect           | Choreography (Our Choice)               | Orchestration                          |
|------------------|-----------------------------------------|----------------------------------------|
| Coordination     | Via domain events                       | Via saga orchestrator                  |
| Coupling         | Loose (services don't know who listens) | Tighter (orchestrator knows all steps) |
| Complexity       | Distributed across services             | Centralized in orchestrator            |
| Failure Handling | Policies in each service                | Orchestrator handles compensations     |
| Fit with Axon    | Natural (event-driven)                  | Requires saga infrastructure           |

We chose **choreography** because:

- It aligns naturally with our event-driven architecture (ADR-002)
- It maintains loose coupling between services
- It leverages Axon's event handling infrastructure
- It's simpler to implement without additional saga infrastructure

### Trade-offs We Accept

**Distributed Logic**:

- Saga logic is spread across multiple services
- **Mitigation**: Clear documentation, consistent patterns, correlation IDs for tracing

**Debugging Complexity**:

- Harder to trace saga flow across services
- **Mitigation**: Centralized logging (Loki), Axon Server dashboard, correlation IDs

**Compensation Complexity**:

- Must carefully design compensating actions for failures
- **Mitigation**: Document compensation logic, test failure scenarios

---

## Alternatives Considered

### Alternative 1: Two-Phase Commit (2PC)

**Approach**: Distributed transaction coordinator ensures all services commit or rollback together

**Pros**:

- ACID consistency across services
- Familiar transaction model

**Cons**:

- Tight coupling between services
- Reduced availability (all services must be up)
- Performance bottleneck (global locks)
- Poor fit for event-driven architecture

**Why rejected**: Creates tight coupling we want to avoid. Reduces availability and scalability. Not compatible with our
event-driven approach.

### Alternative 2: Orchestration Saga

**Approach**: Central saga orchestrator coordinates workflow steps

**Pros**:

- Centralized logic (easier to understand)
- Clear workflow visibility
- Easier failure handling

**Cons**:

- Tighter coupling (orchestrator knows all steps)
- Additional infrastructure
- Single point of coordination

**Why rejected**: Adds complexity we don't need. Choreography fits naturally with our event-driven architecture. We may
reconsider if choreography becomes too complex.

### Alternative 3: Manual Compensation

**Approach**: Handle failures ad-hoc without formal pattern

**Pros**:

- Simple to implement initially
- No pattern overhead

**Cons**:

- Error-prone
- Difficult to maintain
- No consistency across services
- No audit trail

**Why rejected**: Too error-prone. No consistency guarantees. Doesn't scale with system complexity.

---

## Consequences

### Positive

- **Data Consistency**: Maintains consistency across services without distributed transactions
- **Loose Coupling**: Services remain independent
- **Event-Driven**: Natural fit with existing architecture
- **Audit Trail**: Complete event log for saga execution
- **Autonomy**: Each service remains autonomous

### Negative

- **Distributed Logic**: Saga logic spread across services (harder to visualize)
- **Eventual Consistency**: Users may see intermediate states
- **Compensation Design**: Must carefully design compensating actions
- **Testing Complexity**: Requires running multiple services for integration tests
- **Debugging**: Requires event store analysis for failed sagas

### Neutral

- **Event Ordering**: Axon guarantees ordering per aggregate
    - **Note**: Sufficient for our use cases
- **Idempotency**: Event handlers must be idempotent
    - **Note**: Check for existing state before processing

---

## Implementation Details

### Example Saga: Order-to-Delivery-to-Payment Release

**Happy Path**:

```
[Product Selection]
       │
       ├─→ CreateOrderFromCartCommand
       │
       ├─→ OrderCreatedFromCartEvent ────────┐
       │                                      │
       │                           [Payment Service]
       │                              OrderCreatedEventHandler
       │                                      │
       │                                      ├─→ CreatePaymentCommand
       │                                      │
       │                                      ├─→ PaymentCreatedEvent
       │                                      │
       │                           (Customer pays via API)
       │                                      │
       │                                      ├─→ PayOrderCommand
       │                                      │
       │                                      ├─→ PaymentMarkedPaidEvent ───┐
       │                                                                     │
       │                                                      [Delivery Service]
       │                                                 PaymentMarkedPaidEventHandler
       │                                                                     │
       │                                                                     ├─→ CreatePackageDeliveryCommand
       │                                                                     │
       │                                                                     ├─→ Send Email to Baker
       │                                                                     │
       │                                                                     ├─→ PackageDeliveryCreatedEvent
       │                                                                     │
       │                                                          (Baker drops package)
       │                                                                     │
       │                                                                     ├─→ PackageDroppedByBakerEvent ─┐
       │                                                                                                      │
       │                                                                                          [Courier Service]
       │                                                                                           CourierNeededPolicy
       │                                                                                                      │
       │                                                                                                      ├─→ Find nearby couriers
       │                                                                                                      │
       │                                                                                                      ├─→ CreateDeliveryOfferCommand (for each)
       │                                                                                                      │
       │                                                                                                      ├─→ DeliveryOfferCreatedEvent
       │                                                                                                      │
       │                                                                                           (Courier accepts offer)
       │                                                                                                      │
       │                                                                                                      ├─→ AcceptDeliveryOfferCommand
       │                                                                                                      │
       │                                                                                                      ├─→ DeliveryOfferAcceptedEvent ─────┐
       │                                                                                                                                            │
       │                                                                                                                             [Delivery Service]
       │                                                                                                                      DeliveryOfferAcceptedEventHandler
       │                                                                                                                                            │
       │                                                                                                                                            ├─→ AssignCourierCommand
       │                                                                                                                                            │
       │                                                                                                                                 (Courier delivers)
       │                                                                                                                                            │
       │                                                                                                                                            ├─→ PackageRetrievedEvent ──┐
       │                                                                                                                                                                         │
       │                                                                                                                                                              [Payment Service]
       │                                                                                                                                                               PayrollPolicy
       │                                                                                                                                                                         │
       │                                                                                                                                                                         ├─→ ReleaseFundsCommand
       │                                                                                                                                                                         │
       │                                                                                                                                                                         └─→ FundsReleasedEvent
```

### Failure Scenarios and Compensations

1. **Payment Fails**:
    - Event: `PaymentFailedEvent`
    - Compensation: Payment service publishes event → Product Selection cancels order
    - Current Status: Not fully implemented (future enhancement)

2. **No Courier Accepts Offer**:
    - Timeout: Offers expire after configurable period
    - Compensation: Courier service retries offer to other couriers or notifies staff
    - Current Status: Partial implementation (offers can be cancelled)

3. **Courier Doesn't Pick Up Package**:
    - Timeout: Delivery service tracks pickup time
    - Compensation: Re-offer to other couriers, notify baker
    - Current Status: Not implemented (future enhancement)

4. **Customer Doesn't Retrieve Package**:
    - Timeout: Delivery service tracks retrieval time
    - Compensation: Initiate return process, notify customer
    - Current Status: Not implemented (future enhancement)

### Policy Implementation

```kotlin
// Courier Service - CourierNeededPolicy.kt
@Component
class CourierNeededPolicy(
    private val commandGateway: CommandGateway,
    private val courierQueueQueryHandler: CourierQueueQueryHandler
) {
    private val logger = LoggerFactory.getLogger(CourierNeededPolicy::class.java)

    @EventHandler
    fun on(event: PackageDroppedByBakerEvent) {
        logger.info("Package dropped by baker, deliveryId: ${event.deliveryId}, finding nearby couriers...")

        // Query available couriers within 5km
        val nearbyCouriers = courierQueueQueryHandler.findNearbyCouriers(
            event.packageLocation,
            radiusInKm = 5.0
        )

        logger.info("Found ${nearbyCouriers.size} nearby couriers")

        // Create delivery offer for each nearby courier
        nearbyCouriers.forEach { courier ->
            val command = CreateDeliveryOfferCommand(
                offerId = UUID.randomUUID(),
                courierId = courier.courierId,
                deliveryId = event.deliveryId,
                packageLocation = event.packageLocation
            )
            commandGateway.sendAndWait<Any>(command)
            logger.info("Created delivery offer for courier: ${courier.courierId}")
        }

        if (nearbyCouriers.isEmpty()) {
            logger.warn("No nearby couriers found for deliveryId: ${event.deliveryId}")
            // Future: Implement compensation (notify staff, expand radius)
        }
    }
}
```

### Offer Cancellation Policy

```kotlin
// Courier Service - OfferAcceptedPolicy.kt
@Component
class OfferAcceptedPolicy(
    private val commandGateway: CommandGateway,
    private val offerRepository: DeliveryOfferRepository
) {
    @EventHandler
    fun on(event: DeliveryOfferAcceptedEvent) {
        // When one courier accepts, cancel all other pending offers for the same delivery
        val otherPendingOffers = offerRepository.findPendingOffersByDeliveryId(event.deliveryId)
            .filter { it.offerId != event.offerId }

        otherPendingOffers.forEach { offer ->
            commandGateway.sendAndWait<Any>(
                CancelDeliveryOfferCommand(offer.offerId)
            )
        }
    }
}
```

### Saga State Management

Unlike orchestration sagas, choreography sagas don't have explicit saga state. Instead:

- **Aggregate state** tracks the entity's progress through the workflow
- **Event store** provides complete audit trail of saga execution
- **Projections** materialize current saga state for queries

Example: `PackageDelivery` aggregate states:

```kotlin
enum class DeliveryStatus {
    CREATED,           // Delivery created after payment
    DROPPED_BY_BAKER,  // Baker dropped package (saga step 1)
    IN_TRANSIT,        // Courier picked up (saga step 2)
    DROPPED_BY_COURIER,// Courier dropped package (saga step 3)
    RETRIEVED          // Customer retrieved (saga complete, triggers fund release)
}
```

### Idempotency

Sagas must handle duplicate events (due to retries or at-least-once delivery):

```kotlin
@EventHandler
fun on(event: PaymentMarkedPaidEvent) {
    // Check if delivery already exists
    if (deliveryRepository.existsByOrderId(event.orderId)) {
        logger.info("Delivery already exists for orderId: ${event.orderId}, skipping")
        return
    }

    // Create delivery
    commandGateway.sendAndWait<Any>(
        CreatePackageDeliveryCommand(/* ... */)
    )
}
```

### Timeout Handling

For long-running sagas, we use scheduled events:

```kotlin
// Future enhancement: Timeout if courier doesn't pick up
@EventHandler
fun on(event: PackageDroppedByBakerEvent) {
    // Schedule timeout check after 4 hours
    eventScheduler.schedule(
        Duration.ofHours(4),
        DeliveryPickupTimeoutEvent(event.deliveryId)
    )
}

@EventHandler
fun on(event: DeliveryPickupTimeoutEvent) {
    val delivery = queryDelivery(event.deliveryId)
    if (delivery.status == DeliveryStatus.DROPPED_BY_BAKER) {
        // Compensation: Re-offer to couriers or notify staff
        commandGateway.send(EscalateStaleDeliveryCommand(event.deliveryId))
    }
}
```

---

## Testing Strategy

1. **Unit Tests**: Test individual event handlers and policies
2. **Integration Tests**: Use test fixtures to publish events and verify saga progression
3. **End-to-End Tests**: Run full saga flow and verify all services updated correctly
4. **Failure Tests**: Inject failures and verify compensations execute

---

## Monitoring

- Log each saga step with correlation IDs (orderId, deliveryId)
- Prometheus metrics for saga success/failure rates
- Grafana dashboards to track saga duration and bottlenecks
- Axon Server UI to visualize event flows

---

## Compliance

This decision aligns with:

- Event-Driven Architecture (ADR-002) provides the foundation for saga choreography
- CQRS and Event Sourcing (ADR-003) enable saga state tracking
- Database per Service (ADR-006) creates the need for sagas
- Seminar teachings (Axon Framework)

---

## References

- [Microservices Pattern: Saga](https://microservices.io/patterns/data/saga.html)
- [Chris Richardson: Saga Orchestration vs Choreography](https://www.chrisrichardson.net/post/sagas/2019/08/15/developing-sagas-part-3.html)
- [Axon Framework: Sagas](https://docs.axoniq.io/reference-guide/axon-framework/sagas)
- [Martin Fowler: Event-Driven Collaboration](https://martinfowler.com/eaaDev/EventCollaboration.html)
