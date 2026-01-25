# Payment Service Handoff

This repo currently publishes an order message from `product-selection-service` when a user checks out a shopping cart.
The new bounded context/microservice to continue this workflow must be called `payment-service`.

When `product-selection-service` handles checkout:
- HTTP: `POST /product-selection/carts/{cartId}/order`
- Command: `CreateOrderFromCartCommand`
- Published cross-service event (in `shared`): `org.pv293.kotlinseminar.paymentService.events.impl.OrderCreatedFromCartEvent`

## Goal

Implement `payment-service` which reacts to `OrderCreatedFromCartEvent` and continues the ordering/payment flow.

## Contract (already implemented)

The event is defined in:
- `shared/src/main/kotlin/org/pv293/kotlinseminar/paymentService/events/impl/OrderCreatedFromCartEvent.kt`

Payload:
- `orderId: UUID`
- `cartId: UUID`
- `items: List<OrderItemDTO(bakedGoodsId: UUID, quantity: Int)>`

## How to implement payment-service

1) Create the module
- Add new Gradle module: `payment-service`
- Mirror structure from `product-selection-service` (Spring Boot + Axon + JPA)
- Add to `settings.gradle.kts`

2) Wire runtime
- Add `payment-service` + its Postgres to `docker-compose.yml`
- Pick a free host port (e.g. 8085) and a new Postgres port (e.g. 5437)

3) Axon integration
- Configure `axon.axonserver.servers` to point at Axon Server (compose uses `axon-server:8124`)
- Ensure serializers allow `org.pv293.kotlinseminar.**` (same as others)

4) Consume the event
- Add an `@EventHandler` in `payment-service` listening to:
  `org.pv293.kotlinseminar.paymentService.events.impl.OrderCreatedFromCartEvent`
- In the handler, send a `CreatePaymentCommand` (or similarly named) into the payment aggregate.

5) Payment domain
- Create `Payment` aggregate:
  - id should be `orderId` (recommended), or a new `paymentId` that references `orderId`
  - store item list and status (e.g. CREATED -> AUTHORIZED -> CAPTURED/FAILED)
- Persist with JPA (like other services)

6) Expose basic API
- `GET /payment/{orderId}` (or `/payments/{paymentId}`)
- Optional: `POST /payment/{orderId}/authorize`, `POST /payment/{orderId}/capture`

7) Idempotency
Event delivery can be repeated.
- Make `CreatePaymentCommand` idempotent: if a payment for `orderId` already exists, ignore.

## Cleanup after implementation

Once `payment-service` is implemented and verified end-to-end:
1) Delete this file: `PAYMENT_SERVICE_HANDOFF.md`
2) Remove the temporary note that references it from `AGENTS.md`.
