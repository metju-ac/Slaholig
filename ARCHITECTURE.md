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
