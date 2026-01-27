# ADR-001: Microservices Architecture

**Status**: Accepted  
**Date**: 2025-12-24
**Decision Makers**: Development Team  
**Technical Story**: Initial architecture design for Slaholig bakery delivery platform

---

## Context

We need to build a comprehensive bakery goods delivery platform that handles:

- Product catalog and order management
- Payment processing with cryptocurrency
- Physical delivery lifecycle with courier coordination
- Real-time location tracking and geolocation validation

The official assignment allows choosing between **monolithic** and **microservices** architectures. We need to decide
which approach best fits our requirements.

### Key Requirements

1. **Independent Scalability**: Different components have different load profiles (e.g., order browsing vs. payment
   processing)
2. **Technology Flexibility**: Ability to use different technologies per domain (e.g., specialized geolocation
   libraries)
3. **Team Autonomy**: Multiple developers should be able to work independently without conflicts
4. **Fault Isolation**: Failure in one component shouldn't bring down the entire system
5. **Business Domain Separation**: Clear boundaries between product selection, payment, delivery, and courier management

### Assignment Context

- We can choose between monolith and microservices
- The decision is entirely ours
- We need to justify and document our choice

---

## Decision

We have decided to implement a **microservices architecture** with the following service decomposition:

1. **Product Selection Service** - Product catalog, shopping cart, orders
2. **Payment Service** - Payment processing and fund management
3. **Product Delivery Service** - Delivery lifecycle management
4. **Courier Service** - Courier availability and offer matching

Each service:

- Is independently deployable
- Has its own database (database per service pattern)
- Communicates via asynchronous events (event-driven choreography)
- Can be scaled independently
- Owns its domain logic completely

---

## Rationale

### Why Microservices Over Monolith

**Advantages that apply to our use case**:

1. **Domain Complexity**: Our system has 4 distinct bounded contexts with minimal overlap:
    - Product/Order management has different business rules than Payment
    - Delivery tracking has different requirements than Courier matching
    - Clear domain boundaries make service decomposition natural

2. **Scalability Requirements**:
    - Order browsing will have much higher traffic than payment processing
    - Real-time courier location tracking has different scaling characteristics
    - Can scale services independently based on actual load

3. **Technology Fit**:
    - Geolocation features in Courier/Delivery services can use specialized libraries
    - Payment service can integrate with external payment gateways independently
    - Different services can optimize database schemas for their specific access patterns

4. **Fault Isolation**:
    - Payment gateway failure doesn't prevent order browsing
    - Courier matching issues don't block payment processing
    - Better resilience and user experience

5. **Development Velocity**:
    - Multiple team members can work on different services simultaneously
    - Reduced merge conflicts and coordination overhead
    - Faster iteration on individual services

6. **Learning Objectives**:
    - University seminar introduced us to modern distributed systems patterns
    - Microservices better demonstrate our understanding of event-driven architecture
    - More representative of real-world production systems

### Trade-offs We Accept

**Complexity**:

- More moving parts (4 services + infrastructure)
- Requires orchestration (Docker Compose locally, Kubernetes in prod)
- **Mitigation**: Consistent architectural patterns across all services, comprehensive documentation

**Distributed Transactions**:

- No ACID transactions across services
- Need saga pattern for cross-service workflows
- **Mitigation**: Event-driven sagas with Axon Framework, eventual consistency is acceptable for our domain

**Data Consistency**:

- Eventual consistency instead of immediate consistency
- **Mitigation**: Business domain tolerates eventual consistency (e.g., it's acceptable if fund release happens few
  seconds after delivery)

**Operational Overhead**:

- Need monitoring, logging, distributed tracing
- **Mitigation**: Grafana/Prometheus/Loki stack, Axon Server dashboard

**Network Latency**:

- Inter-service communication over network
- **Mitigation**: Asynchronous event-driven communication, no synchronous blocking calls

---

## Alternatives Considered

### Alternative 1: Monolithic Architecture

**Pros**:

- Simpler deployment (single artifact)
- Easier local development
- ACID transactions across entire system
- Lower operational complexity

**Cons**:

- Tight coupling between domains
- Harder to scale specific components
- All-or-nothing deployment (higher risk)
- Limited technology choices (single tech stack)
- Harder for multiple developers to work in parallel

**Why rejected**: Our domain has clear boundaries and different scaling needs. A monolith would sacrifice flexibility
for simplicity we don't need (we have Docker Compose for easy local setup anyway).

### Alternative 2: Modular Monolith

**Pros**:

- Clean domain boundaries within single deployment
- Easier than distributed microservices
- Can refactor to microservices later
- Shared database for easier queries

**Cons**:

- Still single deployment unit
- Can't scale modules independently
- Database contention between modules
- Risk of tight coupling over time

**Why rejected**: Doesn't give us the learning experience with distributed systems we want from this project. If we're
going to have module boundaries anyway, might as well get the benefits of independent deployment and scaling.

### Alternative 3: Serverless Functions (FaaS)

**Pros**:

- Automatic scaling
- Pay-per-use pricing
- No infrastructure management

**Cons**:

- Requires cloud provider (AWS Lambda, Azure Functions)
- Cold start latency
- Vendor lock-in
- Harder to run locally
- Not taught in our seminar

**Why rejected**: Doesn't fit our local-first development approach (Docker Compose). We want full control over
infrastructure and consistency with what we learned in class.

---

## Consequences

### Positive

- **Independent deployment**: Can update Payment service without touching Delivery service
- **Technology flexibility**: Each service can use optimal tools (e.g., PostGIS for geolocation if needed)
- **Clear ownership**: Each service has a clear owner and responsibility
- **Scalability**: Can scale high-traffic services (Product Selection) more than low-traffic (Payment)
- **Resilience**: Isolated failures don't cascade
- **Learning**: Practical experience with distributed systems and event-driven architecture

### Negative

- **Complexity**: More services to develop, test, deploy, and monitor
- **Debugging**: Harder to trace issues across service boundaries
- **Testing**: Need integration tests and contract tests between services
- **Data joins**: Can't do SQL joins across service databases
- **Transaction management**: Need saga pattern for distributed workflows

### Neutral

- **Team size**: Microservices work best with larger teams, we're a small team
    - **Note**: We offset this with consistent architecture patterns and good documentation
- **Event-driven learning curve**: Team needs to learn CQRS, Event Sourcing, and sagas
    - **Note**: We learned this in seminars, so it's actually reinforcing our education

---

## Implementation Notes

### Service Boundaries

Decomposition follows **Domain-Driven Design** bounded contexts:

- **Product Selection**: Shopping, ordering, product catalog
- **Payment**: Financial transactions and fund management
- **Product Delivery**: Physical delivery logistics
- **Courier**: Courier workforce management and matching

### Communication Pattern

Services use **event-driven choreography** via Axon Server:

- No direct HTTP calls between services
- All communication via domain events
- Loose coupling, high cohesion

### Data Management

Each service has its own PostgreSQL database:

- No shared database
- No cross-service foreign keys
- Data duplication is acceptable (e.g., Order info replicated in Payment)

---

## Compliance

This decision aligns with:

- Assignment requirements (choice between monolith and microservices)
- Seminar teachings (Axon Framework, event-driven architecture)
- Industry best practices (Martin Fowler, Sam Newman microservices patterns)

---

## References

- [Microservices.io Patterns](https://microservices.io/patterns/microservices.html)
- [Sam Newman - Building Microservices](https://www.oreilly.com/library/view/building-microservices-2nd/9781492034018/)
- [Martin Fowler - Microservices](https://martinfowler.com/articles/microservices.html)
- [Chris Richardson - Microservices Patterns](https://microservices.io/)
- [Domain-Driven Design by Eric Evans](https://www.domainlanguage.com/ddd/)
