# ADR-006: Spring Boot Framework

**Status**: Accepted  
**Date**: 2025-12-31
**Decision Makers**: Development Team  
**Technical Story**: Application framework for microservices development

---

## Context

Building a microservices platform requires a robust application framework that provides:

- Dependency injection and inversion of control
- REST API development capabilities
- Database access and transaction management
- Configuration management
- Metrics and health monitoring
- Logging infrastructure
- Integration with event-driven frameworks

The Slaholig platform consists of four microservices (Product Selection, Payment, Product Delivery, Courier) that need
consistent patterns for HTTP APIs, database access, and integration with Axon Framework.

### Requirements

1. **Axon Integration**: Must integrate well with Axon Framework (ADR-002)
2. **Production-Ready**: Metrics, health checks, logging out of the box
3. **Developer Experience**: Minimal boilerplate, convention-over-configuration
4. **Ecosystem**: Strong community support and library ecosystem

---

## Decision

We will use **Spring Boot 3.x** as the application framework for all microservices in the Slaholig platform.

**Version**: Spring Boot 3.2.x (or latest stable 3.x)

**Key Dependencies**:

- `spring-boot-starter-web` - REST API development
- `spring-boot-starter-data-jpa` - Database access with Hibernate
- `spring-boot-starter-actuator` - Metrics and health endpoints
- `spring-boot-starter-validation` - Request validation
- `axon-spring-boot-starter` - Axon Framework integration

---

## Rationale

### Why Spring Boot

**1. Axon Framework Integration**:

- Axon provides official Spring Boot starter with auto-configuration
- Automatic setup of CommandGateway, QueryGateway, EventStore
- Aggregate and event handler scanning
- Zero-configuration connection to Axon Server

**2. Kotlin Support**:

- First-class Kotlin support with dedicated documentation
- Extension functions for Spring APIs
- Null-safety alignment

**3. Production-Ready Features**:

- Actuator provides metrics, health checks, application info
- Prometheus metrics export
- Structured logging
- Graceful shutdown

**4. Convention-Over-Configuration**:

- Minimal boilerplate code
- Sensible defaults
- Auto-configuration based on classpath

**5. Mature Ecosystem**:

- Extensive library ecosystem
- Spring Security, Spring Cloud available if needed
- Wide community support

**6. Team Familiarity**:

- Widely adopted in enterprise Java/Kotlin development
- Comprehensive documentation
- Industry-relevant skills

### Spring Boot Features We Use

**1. Dependency Injection**:

```kotlin
@RestController
@RequestMapping("/api/payment")
class PaymentController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway
) {
    // Constructor injection - Spring automatically wires dependencies
}
```

**2. REST API Development**:

```kotlin
@PostMapping("/initiate/{orderId}")
fun initiatePayment(@PathVariable orderId: UUID): PaymentDTO {
    val command = CreatePaymentCommand(orderId)
    commandGateway.sendAndWait<Any>(command)
    return queryPayment(orderId)
}
```

**3. Configuration Management**:

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5437/payment_db
    username: payment_user
    password: payment_pass
  jpa:
    hibernate:
      ddl-auto: update
    
axon:
  axonserver:
    servers: localhost:8024
```

**4. Actuator Endpoints**:

```
GET /actuator/health       - Service health status
GET /actuator/metrics      - Application metrics (Prometheus)
GET /actuator/info         - Application info
GET /actuator/env          - Environment properties
```

**5. Exception Handling**:

```kotlin
@ResponseStatus(HttpStatus.NOT_FOUND)
class NotFoundException(message: String) : RuntimeException(message)

// Spring automatically maps to HTTP 404
```

**6. JPA Integration**:

```kotlin
@Entity
@Table(name = "payments")
data class PaymentProjection(
    @Id
    val paymentId: UUID,
    val orderId: UUID,
    @Enumerated(EnumType.STRING)
    val status: PaymentStatus
)

interface PaymentRepository : JpaRepository<PaymentProjection, UUID>
```

### Trade-offs We Accept

**Memory Footprint**:

- Heavier memory footprint than Quarkus/Micronaut
- **Mitigation**: Acceptable for microservices, not running serverless

**Startup Time**:

- Slower startup than native frameworks
- **Mitigation**: Acceptable for long-running services, not frequently restarted

**Auto-Configuration Magic**:

- Some "magic" can be confusing for newcomers
- **Mitigation**: Documentation, consistent patterns, explicit configuration where needed

---

## Alternatives Considered

### Alternative 1: Quarkus

**Approach**: Kubernetes-native Java framework with GraalVM native image support

**Pros**:

- Faster startup time
- Lower memory footprint
- Native image compilation
- Container-first design

**Cons**:

- Less mature Axon integration
- Smaller ecosystem
- Steeper learning curve
- Less community resources

**Why rejected**: Spring Boot's maturity and official Axon integration outweighed Quarkus performance benefits. Our
services are long-running, so startup time is less critical.

### Alternative 2: Micronaut

**Approach**: Modern framework with compile-time DI

**Pros**:

- Faster startup
- Compile-time dependency injection
- Built for microservices
- Good GraalVM support

**Cons**:

- No official Axon starter
- Smaller community
- Fewer resources and libraries
- Would need custom Axon integration

**Why rejected**: Spring Boot's ecosystem and official Axon support preferred. Team familiarity with Spring.

### Alternative 3: Plain Kotlin + Ktor

**Approach**: Lightweight Kotlin-native web framework

**Pros**:

- Kotlin-first design
- Lightweight
- Modern coroutine support
- Minimal overhead

**Cons**:

- Manual Axon integration
- Less production-ready features
- More boilerplate for enterprise features
- No built-in dependency injection

**Why rejected**: Spring Boot provides more out-of-the-box features. Would need to build infrastructure that Spring
provides.

### Alternative 4: Java EE / Jakarta EE

**Approach**: Standard enterprise Java specification

**Pros**:

- Standard specification
- CDI for dependency injection
- Vendor-neutral

**Cons**:

- Heavyweight
- Complex deployment
- Poor Kotlin support
- Slower development velocity

**Why rejected**: Spring Boot is more lightweight, Kotlin-friendly, and has better Axon integration.

---

## Consequences

### Positive

- **Rapid Development**: Minimal boilerplate through auto-configuration
- **Production-Ready**: Metrics, health checks, logging out of the box
- **Axon Integration**: Seamless integration with event-driven infrastructure
- **Consistent Structure**: Same patterns across all services
- **Easy Onboarding**: Familiar framework for most developers

### Negative

- **Ecosystem Dependency**: Tied to Spring ecosystem (mitigated by clean architecture - ADR-004)
- **Version Upgrades**: Need to stay current with Spring Boot major versions
- **Overhead**: Some overhead compared to lightweight frameworks
- **Magic**: Auto-configuration can be opaque

### Neutral

- **Spring Cloud**: May add modules later (service discovery, circuit breakers)
    - **Note**: Not needed currently, available if required
- **Native Images**: GraalVM support available for future optimization
    - **Note**: Not prioritized currently

---

## Implementation Details

### Gradle Configuration

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"
    kotlin("plugin.jpa") version "1.9.21"
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
}

dependencies {
    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Axon Framework
    implementation("org.axonframework:axon-spring-boot-starter:4.9.1")

    // Database
    runtimeOnly("org.postgresql:postgresql")

    // Kotlin support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Monitoring
    implementation("io.micrometer:micrometer-registry-prometheus")
}
```

### Application Structure

```
src/main/kotlin/org/pv293/kotlinseminar/paymentService/
├── PaymentServiceApplication.kt    # Main entry point
├── controllers/                     # @RestController
├── application/                     # Domain logic
├── repository/                      # @Repository
└── infrastructure/                  # @Configuration
```

### Application Entry Point

```kotlin
@SpringBootApplication
class PaymentServiceApplication

fun main(args: Array<String>) {
    runApplication<PaymentServiceApplication>(*args)
}
```

### Naming Conventions

- Controllers: `*Controller`
- Services: `*Service`
- Repositories: `*Repository`
- Configuration: `*Config`

### Annotation-Based Configuration

- `@SpringBootApplication` - Main application
- `@RestController` - REST endpoints
- `@Service` - Business logic
- `@Repository` - Data access
- `@Configuration` - Infrastructure setup
- `@Component` - Generic Spring bean (for event handlers, policies)

### Constructor Injection (Preferred)

```kotlin
// Good - Constructor injection
class PaymentController(
    private val commandGateway: CommandGateway
)

// Avoid - Field injection
class PaymentController {
    @Autowired
    private lateinit var commandGateway: CommandGateway
}
```

### Axon Integration

Spring Boot's auto-configuration automatically sets up:

- `CommandGateway` for sending commands
- `QueryGateway` for queries
- `EventStore` for event sourcing
- Aggregate and event handler scanning
- Connection to Axon Server

```kotlin
// infrastructure/AxonConfig.kt
@Configuration
class AxonConfig {
    @Bean
    fun configurer(): Configurer {
        return DefaultConfigurer.defaultConfiguration()
    }
}
```

### Profile Configuration

```yaml
# application-dev.yml
spring:
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: create-drop

# application-prod.yml
spring:
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate
```

Run with profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### Production Features

**Health Checks**:

```yaml
management:
  endpoint:
    health:
      show-details: always
  health:
    db:
      enabled: true
```

**Metrics Export** (Prometheus):

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**Logging**:

```yaml
logging:
  level:
    org.pv293.kotlinseminar: INFO
    org.axonframework: DEBUG
```

**Graceful Shutdown**:

```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

### Docker Integration

```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
payment-service:
  build:
    context: ./payment-service
  ports:
    - "8081:8081"
  environment:
    SPRING_PROFILES_ACTIVE: prod
    SPRING_DATASOURCE_URL: jdbc:postgresql://payment-postgres:5432/payment_db
    AXON_AXONSERVER_SERVERS: axonserver:8124
```

---

## Future Considerations

1. **Spring Cloud**: May add modules if we need:
    - Service discovery (Eureka)
    - API Gateway (Spring Cloud Gateway)
    - Circuit breaker (Resilience4j)
    - Distributed tracing (Sleuth)

2. **Spring Boot 3.x Features**:
    - Native image compilation with GraalVM
    - Improved observability with Micrometer tracing
    - Virtual threads (Project Loom) for better concurrency

3. **Upgrade Strategy**:
    - Stay within Spring Boot 3.x minor versions
    - Plan major version upgrades carefully
    - Use deprecation warnings to prepare for changes

---

## Compliance

This decision aligns with:

- Microservices Architecture (ADR-001) benefits from Spring Boot's lightweight deployment
- Event-Driven Architecture (ADR-002) requires Axon integration, which Spring Boot provides
- Clean Architecture (ADR-004) keeps business logic independent of Spring Boot
- Seminar teachings (Spring Boot with Kotlin)

---

## References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Boot with Kotlin](https://spring.io/guides/tutorials/spring-boot-kotlin/)
- [Axon Spring Boot Starter](https://docs.axoniq.io/reference-guide/axon-framework/spring-boot-integration)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Spring Boot Best Practices](https://www.baeldung.com/spring-boot-best-practices)
