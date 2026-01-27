# ADR-006: Database per Service Pattern

**Status**: Accepted  
**Date**: 2025-12-31
**Decision Makers**: Development Team  
**Technical Story**: Data persistence strategy for microservices architecture

---

## Context

In a microservices architecture, one of the key decisions is how to manage data persistence. Each service needs to store
and retrieve data, but the approach to database design significantly impacts service autonomy, scalability, and data
consistency.

The Slaholig platform consists of multiple services (Product Selection, Payment, Product Delivery, Courier) that manage
distinct business domains with their own data models.

### Requirements

1. **Service Autonomy**: Services should be able to evolve their data models independently
2. **Independent Deployment**: Schema changes shouldn't require coordination across services
3. **Scalability**: Different services have different data volume and access patterns
4. **Fault Isolation**: Database issues in one service shouldn't affect others
5. **Clear Ownership**: Each service should own its data completely

### Key Question

Should we use:

- A shared database accessed by all services?
- Separate databases per service?
- Separate schemas in a shared database instance?

---

## Decision

We will adopt the **Database per Service** pattern. Each microservice will have its own dedicated PostgreSQL database
instance that is accessed exclusively by that service.

### Service Database Allocation

| Service           | Database Name        | Port |
|-------------------|----------------------|------|
| Product Selection | product_selection_db | 5436 |
| Payment           | payment_db           | 5437 |
| Product Delivery  | product_delivery_db  | 5438 |
| Courier           | courier_db           | 5439 |

### Core Principles

1. **Exclusive Access**: No service can directly access another service's database
2. **Schema Ownership**: Each service owns its schema evolution (via Hibernate DDL or Flyway)
3. **Event-Driven Data Sharing**: Cross-service data access occurs only via events and queries through Axon Server
4. **Isolated Deployment**: Each database is provisioned and deployed with its service

---

## Rationale

### Why Database per Service

**1. Service Autonomy**:

```
Shared Database:
[Product Service] ────┐
                      ├──> [Shared DB]
[Payment Service] ────┘
Problem: Schema changes in Product tables might break Payment queries

Database per Service:
[Product Service] ──> [Product DB]
[Payment Service] ──> [Payment DB]
Solution: Each service controls its own schema completely
```

**2. Technology Flexibility**:

- Each service can choose optimal database technology (though we use PostgreSQL consistently)
- Can use service-specific features (e.g., PostGIS for geolocation in Courier service)
- Different indexing strategies per service

**3. Scalability**:

- Scale databases independently based on service load
- Product Selection DB can be read-heavy replica set
- Payment DB can prioritize write consistency
- No cross-service lock contention

**4. Fault Isolation**:

- Payment database crash doesn't prevent order browsing
- Database maintenance on one service doesn't affect others
- Better overall system availability

**5. Clear Bounded Contexts**:

- Physical database boundaries reinforce logical domain boundaries
- Prevents "convenience queries" that bypass service boundaries
- Forces proper event-driven data synchronization

### Data Consistency Strategy

Since we've adopted CQRS and Event Sourcing (ADR-003), eventual consistency is already part of our architecture:

**1. Write Operations**: Commands update a single service's aggregate and database

**2. Read Operations**: Event handlers build projections in each service's database

**3. Cross-Service Data**: Services subscribe to relevant events to maintain local read models

**4. Event Store**: Axon Server's event store serves as the single source of truth

**Example Flow - Order Creation**:

```
1. Product Selection Service creates order
   └─> Writes to product_selection_db
   
2. Publishes OrderCreatedFromCartEvent to Axon Server
   
3. Payment Service listens to event
   └─> Creates payment record in payment_db
   
4. Delivery Service listens to event  
   └─> Creates delivery record in product_delivery_db

Result: Each service maintains only the data it needs
```

### Trade-offs We Accept

**Data Duplication**:

- Order information exists in Product Selection, Payment, and Delivery databases
- **Mitigation**: Each service stores only what it needs; events are source of truth

**Distributed Queries**:

- Can't JOIN across service databases
- **Mitigation**: Build projections via event handlers; compose queries at API gateway

**Eventual Consistency**:

- Cross-service data synchronization is eventually consistent
- **Mitigation**: Our business domain tolerates this (few seconds delay is acceptable)

**Operational Complexity**:

- More database instances to monitor, backup, and maintain
- **Mitigation**: Docker Compose for local dev; Prometheus monitoring; automated backups

---

## Alternatives Considered

### Alternative 1: Shared Database

**Approach**: All services access a single shared database

**Pros**:

- Easy to join data across domains
- ACID transactions across entire system
- Single database to manage and backup
- Immediate consistency

**Cons**:

- Tight coupling between services
- Schema changes in one service can break others
- Database becomes contention point
- Services can't be deployed independently
- Microservices architecture degrades to distributed monolith

**Why rejected**: Violates core microservices principle of service autonomy. Creates hidden dependencies through
database schema. If we wanted shared database, we should build a monolith instead.

### Alternative 2: Shared Database with Service-Specific Schemas

**Approach**: Single database instance with separate schemas per service

**Example**:

```
PostgreSQL Instance:
├── product_selection_schema
├── payment_schema
├── delivery_schema
└── courier_schema
```

**Pros**:

- Some isolation between services
- Easier than multiple database instances
- Single connection pool management

**Cons**:

- Still deployment dependency (database migrations must coordinate)
- Schema-level permissions harder to enforce
- Can't scale databases independently
- Temptation to bypass service boundaries

**Why rejected**: Provides some isolation but still creates deployment coupling. Doesn't give us the scalability
benefits. If we're embracing microservices, go all the way.

### Alternative 3: API-Based Data Access

**Approach**: No event-driven data replication; services query each other via REST APIs

**Example**:

```
Delivery Service needs order info:
  Delivery Service --HTTP GET--> Product Selection Service
```

**Pros**:

- No data duplication
- Immediate consistency
- Single source of truth per entity

**Cons**:

- Synchronous coupling between services
- Service availability dependencies
- Higher latency for queries
- Cascading failures
- Violates event-driven architecture

**Why rejected**: Creates tight runtime coupling we're trying to avoid with event-driven architecture (ADR-002).
Synchronous dependencies reduce resilience.

---

## Consequences

### Positive

- **Independent Evolution**: Services can evolve schemas without cross-service coordination
- **Scalability**: Each database can be scaled independently (replicas, sharding)
- **Fault Isolation**: Database issues contained to single service
- **Clear Ownership**: No ambiguity about data responsibility
- **Technology Flexibility**: Can use different database technologies per service if needed
- **Deployment Independence**: Schema migrations deploy with service

### Negative

- **Operational Overhead**: 4+ database instances to manage, monitor, and backup
- **Data Duplication**: Order/delivery information replicated across services
- **Eventual Consistency**: Cross-service data not immediately consistent
- **Distributed Transactions**: Need Saga pattern (ADR-007) for cross-service workflows
- **Testing Complexity**: Integration tests require multiple database instances

### Neutral

- **Database Costs**: More instances but can size each appropriately
    - **Note**: In cloud, might cost more; locally via Docker Compose, same cost
- **Learning Curve**: Team needs to understand eventual consistency patterns
    - **Note**: We learned this in seminar, so it's educational reinforcement

---

## Implementation Notes

### Local Development

**Docker Compose Configuration**:

```yaml
# docker-compose.yml (excerpt)
payment-postgres:
  image: postgres:17
  environment:
    POSTGRES_DB: payment_db
    POSTGRES_USER: payment_user
    POSTGRES_PASSWORD: payment_pass
  ports:
    - "5437:5432"
  volumes:
    - payment_postgres_data:/var/lib/postgresql/data
  healthcheck:
    test: [ "CMD-SHELL", "pg_isready -U payment_user" ]
    interval: 10s
    timeout: 5s
    retries: 5
```

### Connection Management

Each service uses **HikariCP** connection pooling:

```yaml
# application.yml (Payment Service)
spring:
  datasource:
    url: jdbc:postgresql://payment-postgres:5432/payment_db
    username: payment_user
    password: payment_pass
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
```

### Schema Management

**Development**: Hibernate DDL auto-update

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

**Production**: Flyway migrations (recommended)

```kotlin
// Future: V001__create_payment_table.sql
CREATE TABLE payments (
    payment_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

### Backup Strategy

Each database needs independent backup:

- Docker volumes for data persistence
- Automated daily backups via cron
- Point-in-time recovery capability
- Per-service backup retention policies

### Monitoring

Prometheus metrics track per-service:

- Connection pool usage
- Query performance
- Database size
- Transaction rates

---

## Migration Strategy

If this were a brownfield project migrating from shared database:

1. **Identify Ownership**: Map tables to service boundaries
2. **Separate Schemas**: Create schemas within shared database
3. **Event-Driven Sync**: Implement event handlers for cross-service data
4. **Remove Foreign Keys**: Break cross-service FK relationships
5. **Physical Separation**: Move schemas to separate database instances
6. **Verify Sync**: Ensure event-driven data replication works
7. **Cleanup**: Remove legacy cross-service queries

---

## Compliance

This decision aligns with:

- Microservices architecture (ADR-001)
- Event-Driven Architecture (ADR-002)
- CQRS and Event Sourcing (ADR-003)
- Industry best practices (Database per Service pattern)

---

## References

- [Microservices Pattern: Database per Service](https://microservices.io/patterns/data/database-per-service.html)
- [Martin Fowler: Data Management in Microservices](https://martinfowler.com/articles/microservices.html#DecentralizedDataManagement)
- [Chris Richardson: Microservices Data Patterns](https://www.chrisrichardson.net/post/microservices/patterns/2017/07/12/developing-transactional-microservices-using-aggregates-and-event-sourcing.html)
- [Sam Newman: Building Microservices - Chapter 4: Modeling Services](https://www.oreilly.com/library/view/building-microservices-2nd/9781492034018/)
