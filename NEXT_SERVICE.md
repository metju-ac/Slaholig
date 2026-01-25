# Implementing product-delivery-service

## Overview

The `product-delivery-service` is responsible for handling order fulfillment after successful payment. It listens to payment events and coordinates the delivery process by notifying the baker to prepare and ship the baked goods.

**Primary Responsibility**: Listen to `PaymentMarkedPaidEvent` and send a mock email notification to the baker.

## Architecture Context

```
[Payment Service] --emits--> PaymentMarkedPaidEvent
                                    |
                                    v
                     [Product Delivery Service] --sends--> Email to Baker (mocked)
```

## Event to Listen For

**Event**: `PaymentMarkedPaidEvent`
**Location**: `shared/src/main/kotlin/org/pv293/kotlinseminar/paymentService/events/impl/PaymentMarkedPaidEvent.kt`

```kotlin
data class PaymentMarkedPaidEvent(
    val orderId: UUID,
    val transactionId: String,
)
```

This event is published by the `payment-service` when a payment is successfully processed.

## Step-by-Step Implementation Guide

### Step 1: Add Service to Gradle Settings

**File**: `settings.gradle.kts`

Add the new module:
```kotlin
include("product-delivery-service")
```

**Reference**: See line 8-9 in `settings.gradle.kts` for existing services.

---

### Step 2: Create build.gradle.kts

**File**: `product-delivery-service/build.gradle.kts`

Copy from `payment-service/build.gradle.kts` and update the main class:

```kotlin
plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.spring)
    alias(libs.plugins.springboot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jpa)
    alias(libs.plugins.noarg)
}

group = "org.pv293"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.jackson)
    implementation(libs.kotlin.reflect)
    runtimeOnly(libs.micrometer.prometheus)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.launcher)
    implementation(libs.axon)
    implementation(libs.axon.kotlin)
    implementation(libs.axon.spring)
    implementation(libs.axon.spring.boot.starter)
    implementation(libs.kotlinx.datetime)
    implementation(libs.springboot.starter.data.jpa)
    implementation(libs.h2.database)
    implementation(libs.postgres)
    implementation(libs.logback.classic)
    implementation(libs.logback.loki.appender)
    implementation(libs.logstash.logback.encoder)
    implementation(libs.springdoc.openapi.ui)
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("org.pv293.kotlinseminar.productDeliveryService.ProductDeliveryServiceApplicationKt")
}
```

---

### Step 3: Create Directory Structure

Create the following directory structure:

```
product-delivery-service/
├── build.gradle.kts
├── Dockerfile
└── src/
    └── main/
        ├── kotlin/
        │   └── org/
        │       └── pv293/
        │           └── kotlinseminar/
        │               └── productDeliveryService/
        │                   ├── ProductDeliveryServiceApplication.kt
        │                   ├── application/
        │                   │   ├── policies/
        │                   │   │   └── DeliveryPolicies.kt
        │                   │   └── services/
        │                   │       └── EmailNotificationService.kt
        │                   └── infrastructure/
        │                       └── AxonConfig.kt
        └── resources/
            ├── application-dev.yml
            ├── application-prod.yml
            └── logback-spring.xml
```

---

### Step 4: Create Application Entry Point

**File**: `product-delivery-service/src/main/kotlin/org/pv293/kotlinseminar/productDeliveryService/ProductDeliveryServiceApplication.kt`

```kotlin
package org.pv293.kotlinseminar.productDeliveryService

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ProductDeliveryServiceApplication

fun main(args: Array<String>) {
    runApplication<ProductDeliveryServiceApplication>(*args)
}
```

**Reference**: `payment-service/src/main/kotlin/org/pv293/kotlinseminar/paymentService/PaymentServiceApplication.kt`

---

### Step 5: Create the Policy (Event Handler)

**File**: `product-delivery-service/src/main/kotlin/org/pv293/kotlinseminar/productDeliveryService/application/policies/DeliveryPolicies.kt`

```kotlin
package org.pv293.kotlinseminar.productDeliveryService.application.policies

import org.axonframework.eventhandling.EventHandler
import org.pv293.kotlinseminar.paymentService.events.impl.PaymentMarkedPaidEvent
import org.pv293.kotlinseminar.productDeliveryService.application.services.EmailNotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DeliveryPolicies(
    private val emailNotificationService: EmailNotificationService,
) {
    private val logger = LoggerFactory.getLogger(DeliveryPolicies::class.java)

    @EventHandler
    fun on(event: PaymentMarkedPaidEvent) {
        logger.info("Received PaymentMarkedPaidEvent for order ${event.orderId}, transaction ${event.transactionId}")
        
        // Send mock email notification to baker
        emailNotificationService.notifyBaker(
            orderId = event.orderId,
            transactionId = event.transactionId,
        )
        
        logger.info("Baker notification sent for order ${event.orderId}")
    }
}
```

**Reference Pattern**: `product-selection-service/src/main/kotlin/org/pv293/kotlinseminar/productSelectionService/application/policies/ShoppingCartPolicies.kt:48-54`

**Key Points**:
- Use `@Component` to make it a Spring bean
- Use `@EventHandler` on the method that handles the event
- The method parameter type determines which event it listens to
- No `@ProcessingGroup` needed unless you want to customize event processing configuration

---

### Step 6: Create Mock Email Notification Service

**File**: `product-delivery-service/src/main/kotlin/org/pv293/kotlinseminar/productDeliveryService/application/services/EmailNotificationService.kt`

```kotlin
package org.pv293.kotlinseminar.productDeliveryService.application.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EmailNotificationService {
    private val logger = LoggerFactory.getLogger(EmailNotificationService::class.java)
    
    fun notifyBaker(orderId: UUID, transactionId: String) {
        // Mock email sending - in production this would integrate with an email service
        logger.info("========================================")
        logger.info("📧 MOCK EMAIL TO BAKER")
        logger.info("========================================")
        logger.info("To: baker@slaholig.cz")
        logger.info("Subject: New Order Ready for Delivery")
        logger.info("")
        logger.info("Dear Baker,")
        logger.info("")
        logger.info("A new order has been successfully paid and is ready for preparation:")
        logger.info("")
        logger.info("  Order ID:       $orderId")
        logger.info("  Transaction ID: $transactionId")
        logger.info("")
        logger.info("Please prepare the baked goods and arrange for delivery.")
        logger.info("")
        logger.info("Best regards,")
        logger.info("Slaholig Automated System")
        logger.info("========================================")
    }
}
```

**Note**: This is a mock implementation. In production, you would integrate with an actual email service (SendGrid, AWS SES, etc.).

---

### Step 7: Create Axon Configuration (Optional but Recommended)

**File**: `product-delivery-service/src/main/kotlin/org/pv293/kotlinseminar/productDeliveryService/infrastructure/AxonConfig.kt`

```kotlin
package org.pv293.kotlinseminar.productDeliveryService.infrastructure

import org.axonframework.eventhandling.EventBus
import org.springframework.context.annotation.Configuration

@Configuration
class AxonConfig {
    // Add any Axon-specific configuration here if needed
    // For now, Spring Boot auto-configuration handles everything
}
```

**Reference**: `payment-service/src/main/kotlin/org/pv293/kotlinseminar/paymentService/infrastructure/AxonConfig.kt`

---

### Step 8: Create Application Configuration Files

#### **File**: `product-delivery-service/src/main/resources/application-prod.yml`

```yaml
spring:
  application:
    name: product-delivery-service
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

  datasource:
    url: jdbc:postgresql://postgres-product-delivery:5432/db
    driver-class-name: org.postgresql.Driver
    username: admin
    password: admin
    jackson:
      property-naming-strategy: SNAKE_CASE

server:
  port: 8080

axon:
  axonserver:
    servers: axon-server:8124

management:
  endpoints:
    web:
      exposure:
        include:
          - health
          - metrics
          - prometheus

logging:
  loki:
    url: http://loki:3100
```

#### **File**: `product-delivery-service/src/main/resources/application-dev.yml`

```yaml
spring:
  application:
    name: product-delivery-service
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect

  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: admin
    password: admin

  h2:
    console:
      enabled: true

server:
  port: 8086

axon:
  axonserver:
    servers: localhost:8124

management:
  endpoints:
    web:
      exposure:
        include:
          - health
          - metrics
          - prometheus
```

#### **File**: `product-delivery-service/src/main/resources/logback-spring.xml`

Copy from `payment-service/src/main/resources/logback-spring.xml` (content is identical across services).

**Reference**: `payment-service/src/main/resources/application-prod.yml` and `application-dev.yml`

---

### Step 9: Create Dockerfile

**File**: `product-delivery-service/Dockerfile`

```dockerfile
# Stage 1: Build
FROM gradle:8.10-jdk21-corretto AS build

WORKDIR /application

# Copy Gradle build files from root
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Copy shared module (required dependency)
COPY shared ./shared

# Copy this service
COPY product-delivery-service ./product-delivery-service

# Build only this service
RUN gradle :product-delivery-service:clean :product-delivery-service:bootJar --no-daemon

# Verify the output
RUN ls -l product-delivery-service/build/libs

# Stage 2: Runtime
FROM amazoncorretto:21.0.9-alpine AS run

RUN adduser -D -s /bin/false app-user

# Copy the built jar
COPY --from=build --chown=app-user:app-user /application/product-delivery-service/build/libs/*.jar app.jar

# Copy axon config from root
COPY axon ./axon

EXPOSE 8080

USER app-user
CMD ["java", "-jar", "app.jar", "--spring.config.additional-location=/config/product-delivery-service/"]
```

**Reference**: `payment-service/Dockerfile`

---

### Step 10: Update docker-compose.yml

**File**: `docker-compose.yml`

Add the following sections:

#### 1. Add PostgreSQL database (after line 89):

```yaml
  postgres-product-delivery:
    image: postgres:17
    ports:
      - 5438:5432
    environment:
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin
      POSTGRES_DB: db
```

#### 2. Add the service (after line 199):

```yaml
  product-delivery-service:
    image: kotlin-seminar/product-delivery-service
    build:
      context: .
      dockerfile: product-delivery-service/Dockerfile
    ports:
      - 8086:8080
    depends_on:
      - axon-server
      - postgres-product-delivery
      - loki
      - prometheus
    environment:
      spring_profiles_active: prod
      LOKI_ADD: loki
      LOKI_PORT: 3100
    volumes:
      - ./spring/config:/config
```

**Note**: Port 8086 is the next available port (payment-service uses 8085).

**Reference**: `docker-compose.yml:182-199` (payment-service configuration)

---

### Step 11: Update prometheus.yml

**File**: `prometheus.yml`

Add the following scrape config (after line 39):

```yaml
  - job_name: 'product-delivery-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['product-delivery-service:8080']
        labels:
          service: 'product-delivery-service'
```

**Reference**: `prometheus.yml:33-38` (product-selection-service configuration)

---

### Step 12: Update README.md

**File**: `README.md`

Add documentation for the new service after the Product Selection Service section (after line 37):

```markdown
### Product Delivery Service
- **Swagger UI**: http://localhost:8086/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8086/api-docs
```

**Reference**: `README.md:35-37` (Product Selection Service section)

---

## Building and Testing

### Build the Service

From the project root:

```bash
./gradlew :product-delivery-service:build
```

### Run Locally (Development Mode)

```bash
./gradlew :product-delivery-service:bootRun --args='--spring.profiles.active=dev'
```

The service will start on port 8086 and connect to Axon Server at localhost:8124.

### Run with Docker Compose

```bash
docker compose up --build product-delivery-service
```

Or start the entire stack:

```bash
docker compose up --build
```

### Testing the Integration

1. **Start all services** via Docker Compose
2. **Create a shopping cart** and add items (via product-selection-service)
3. **Create an order** from the cart (via product-selection-service)
4. **Mark the payment as paid** (via payment-service)
5. **Check the logs** for product-delivery-service:

```bash
docker compose logs -f product-delivery-service
```

You should see the mock email notification in the logs when a payment is marked as paid.

### Expected Log Output

```
INFO  DeliveryPolicies - Received PaymentMarkedPaidEvent for order <uuid>, transaction <transaction-id>
INFO  EmailNotificationService - ========================================
INFO  EmailNotificationService - 📧 MOCK EMAIL TO BAKER
INFO  EmailNotificationService - ========================================
INFO  EmailNotificationService - To: baker@slaholig.cz
...
INFO  DeliveryPolicies - Baker notification sent for order <uuid>
```

---

## Key Reference Files

Study these files for patterns and examples:

1. **Event Handler/Policy Pattern**:
   - `product-selection-service/src/main/kotlin/org/pv293/kotlinseminar/productSelectionService/application/policies/ShoppingCartPolicies.kt`
   - `payment-service/src/main/kotlin/org/pv293/kotlinseminar/paymentService/events/handlers/OrderCreatedEventHandler.kt`

2. **Service Setup**:
   - `payment-service/build.gradle.kts`
   - `payment-service/Dockerfile`
   - `payment-service/src/main/kotlin/org/pv293/kotlinseminar/paymentService/PaymentServiceApplication.kt`

3. **Configuration**:
   - `payment-service/src/main/resources/application-prod.yml`
   - `docker-compose.yml` (payment-service and product-selection-service sections)

4. **Shared Events**:
   - `shared/src/main/kotlin/org/pv293/kotlinseminar/paymentService/events/impl/PaymentMarkedPaidEvent.kt`

---

## Architecture Notes

### Event-Driven Flow

```
1. User completes purchase
2. Product Selection Service → emits OrderCreatedFromCartEvent
3. Payment Service → listens to OrderCreatedFromCartEvent
4. Payment Service → creates payment
5. User/Admin marks payment as paid
6. Payment Service → emits PaymentMarkedPaidEvent
7. Product Delivery Service → listens to PaymentMarkedPaidEvent
8. Product Delivery Service → sends notification to baker (mocked)
```

### Why Use Policies vs Event Handlers?

- **Policies**: React to events and dispatch commands (orchestration logic)
- **Event Handlers**: Update projections/read models or perform side effects

In this case, `DeliveryPolicies` is appropriate because we're performing a side effect (sending an email) based on an event from another service.

---

## Common Issues and Solutions

### Issue: Event Not Being Received

**Solution**: 
- Verify Axon Server is running: http://localhost:8024
- Check that all services are connected to the same Axon Server instance
- Ensure the event class is in the `shared` module and imported correctly
- Check Axon Server dashboard for event subscriptions

### Issue: Build Fails

**Solution**:
- Run `./gradlew clean` first
- Ensure JDK 21 is installed: `java -version`
- Verify all dependencies in `build.gradle.kts` are correct

### Issue: Service Won't Start in Docker

**Solution**:
- Check logs: `docker compose logs product-delivery-service`
- Verify database is ready: `docker compose ps postgres-product-delivery`
- Ensure port 8086 is not in use: `lsof -i :8086` (on macOS/Linux)

---

## Optional Enhancements

Once the basic implementation is working, consider:

1. **Add a Delivery Entity**: Track delivery status in a database
2. **Create Commands**: Add commands like `ScheduleDeliveryCommand`, `MarkAsDeliveredCommand`
3. **Emit Events**: Publish `DeliveryScheduledEvent`, `DeliveryCompletedEvent`
4. **Add REST Controller**: Expose endpoints to query delivery status
5. **Add Saga**: Coordinate multi-step delivery process with rollback capability
6. **Real Email Integration**: Replace mock with actual email service (e.g., SendGrid)

---

## Questions or Issues?

If you encounter problems:

1. Check the AGENTS.md file for general development guidelines
2. Review the reference files listed above
3. Check Axon Framework documentation: https://docs.axoniq.io/home/
4. Verify your implementation matches the existing service patterns

Good luck! 🚀
