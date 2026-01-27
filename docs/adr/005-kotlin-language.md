# ADR-005: Kotlin as Primary Language

**Status**: Accepted  
**Date**: 2025-12-24  
**Decision Makers**: Development Team  
**Technical Story**: Programming language selection for all services

---

## Context

We need to choose a programming language for building our microservices platform. The project runs on the JVM (Java
Virtual Machine) as required by our use of Spring Boot and Axon Framework.

### Requirements

1. **JVM Compatibility**: Must run on JVM (Axon Framework requirement)
2. **Spring Boot Support**: Must integrate well with Spring Boot ecosystem
3. **Type Safety**: Prevent runtime errors through static typing
4. **Developer Productivity**: Concise, expressive code
5. **Modern Features**: Support for functional programming, null safety
6. **Learning Value**: Industry-relevant skills
7. **Axon Compatibility**: First-class support for Axon Framework

### Seminar Context

Our university seminars showcased Kotlin as a modern JVM language. We were introduced to its features and saw it used
with Spring Boot and Axon Framework. We found it to be elegant, concise, and expressive.

---

## Decision

We will use **Kotlin** as the primary programming language for all microservices.

**Specifics**:

- Kotlin version: 1.9+ (latest stable)
- JVM target: Java 21
- Build tool: Gradle with Kotlin DSL
- All services written in Kotlin
- Shared module written in Kotlin

---

## Rationale

### Why Kotlin

**1. Modern Language Features**:

Null Safety:

```kotlin
// Kotlin - null safety built-in
var payment: Payment? = null           // Explicit nullable
payment.process()                      // Compile error
payment?.process()                     // Safe call
payment!!.process()                    // Explicit non-null assertion

// Java - no null safety
Payment payment = null;                // Can be null
payment.process();                     // NullPointerException at runtime
```

**2. Concise Syntax**:

Data Classes:

```kotlin
// Kotlin - one line
data class PaymentDTO(val paymentId: UUID, val amount: BigDecimal, val status: String)

// Auto-generates: equals(), hashCode(), toString(), copy()

// Java - 30+ lines
public class PaymentDTO {
    private final UUID paymentId;
    private final BigDecimal amount;
    private final String status;

    public PaymentDTO(UUID paymentId, BigDecimal amount, String status)
    {
        this.paymentId = paymentId;
        this.amount = amount;
        this.status = status;
    }

    public UUID getPaymentId()
    { return paymentId; }
    public BigDecimal getAmount()
    { return amount; }
    public String getStatus()
    { return status; }

    // equals(), hashCode(), toString() - 20 more lines
}
```

**3. Functional Programming**:

```kotlin
// Kotlin - functional style
val availableCouriers = couriers
    .filter { it.isAvailable }
    .filter { calculateDistance(it.location, packageLocation) < 5000 }
    .sortedBy { calculateDistance(it.location, packageLocation) }
    .take(10)

// Java - imperative style
List<Courier> availableCouriers = new ArrayList<>();
for (Courier courier : couriers) {
    if (courier.isAvailable()) {
        double distance = calculateDistance (courier.getLocation(), packageLocation);
        if (distance < 5000) {
            availableCouriers.add(courier);
        }
    }
}
availableCouriers.sort(Comparator.comparing(c ->
calculateDistance(c.getLocation(), packageLocation)));
availableCouriers = availableCouriers.subList(0, Math.min(10, availableCouriers.size()));
```

**4. Spring Boot Integration**:

Kotlin-specific features:

```kotlin
// Constructor injection (no @Autowired needed)
@RestController
class PaymentController(
    private val commandGateway: CommandGateway,  // Injected
    private val queryGateway: QueryGateway       // Injected
) {
    // Clean, concise
}

// Companion objects for logger
companion object {
    private val logger = LoggerFactory.getLogger(PaymentController::class.java)
}
```

**5. String Templates**:

```kotlin
// Kotlin
logger.info("Payment $paymentId succeeded for order $orderId with amount $amount")

// Java
logger.info(
    String.format(
        "Payment %s succeeded for order %s with amount %s",
        paymentId, orderId, amount
    )
);
```

**6. Extension Functions**:

```kotlin
// Extend existing classes
fun UUID.isValid(): Boolean = this.toString().matches(UUID_REGEX)

// Use anywhere
if (paymentId.isValid()) {
    ...
}
```

**7. Coroutines** (future):

```kotlin
// Async/await style (if we need it)
suspend fun processPayment(paymentId: UUID): PaymentResult {
    val validation = async { validatePayment(paymentId) }
    val gateway = async { callPaymentGateway(paymentId) }
    return PaymentResult(validation.await(), gateway.await())
}
```

**8. Null Safety at Scale**:

Prevents NullPointerException (billion-dollar mistake):

```kotlin
// Kotlin compiler forces null handling
val payment: Payment? = repository.findById(id)
payment.process()  // Compile error: Payment? can't be used as Payment

// Must handle null explicitly
payment?.process()  // Safe call (do nothing if null)
payment ?: throw NotFoundException("Payment not found")  // Elvis operator
```

**9. Immutability by Default**:

```kotlin
val immutable = "can't change"         // Immutable (final in Java)
var mutable = "can change"             // Mutable (rare)

// Data classes are immutable by design
data class Payment(val id: UUID, val amount: BigDecimal)
```

**10. Smart Casts**:

```kotlin
// Kotlin - automatic casting after type check
if (result is PaymentSucceeded) {
    logger.info("Amount: ${result.amount}")  // result auto-cast
}

// Java - manual casting
if (result instanceof PaymentSucceeded) {
    logger.info("Amount: " + ((PaymentSucceeded) result).getAmount());
}
```

---

## Alternatives Considered

### Alternative 1: Java

**Pros**:

- Most widely used JVM language
- Largest ecosystem
- More developers know Java
- Maximum compatibility

**Cons**:

- Verbose (data classes, getters/setters, builders)
- No null safety (until Project Valhalla)
- More boilerplate
- Less expressive
- Older language design (pre-2011)

**Example Comparison**:

```kotlin
// Kotlin - 3 lines
data class PaymentCreatedEvent(val paymentId: UUID, val orderId: UUID, val amount: BigDecimal)

// Java 17 Record - 3 lines (similar!)
public record PaymentCreatedEvent(UUID paymentId, UUID orderId, BigDecimal amount) {}

// But for Spring Boot controller:

// Kotlin
@RestController
class PaymentController(private val gateway: CommandGateway) {
    @PostMapping("/pay")
    fun pay(@RequestBody req: PayRequest) =
        gateway.sendAndWait<Any>(PayOrderCommand(req.paymentId))
}

// Java
@RestController
public class PaymentController {
    private final CommandGateway gateway;

    @Autowired
    public PaymentController(CommandGateway gateway)
    {
        this.gateway = gateway;
    }

    @PostMapping("/pay")
    public void pay(@RequestBody PayRequest req)
    {
        gateway.sendAndWait(new PayOrderCommand (req.getPaymentId()));
    }
}
```

**Why not chosen**:

- Kotlin is more concise and modern
- Null safety prevents bugs
- Better developer experience
- We learned Kotlin in seminars
- Still 100% compatible with Java libraries

### Alternative 2: Scala

**Pros**:

- Powerful type system
- Excellent functional programming
- Akka framework (actor model)
- Very expressive

**Cons**:

- Steep learning curve
- Slower compilation
- Complex type system (can be overwhelming)
- Less Spring Boot integration
- Not taught in our seminar

**Why not chosen**:

- Too complex for our needs
- Team not familiar with Scala
- Slower compilation hurts iteration speed
- Kotlin gives us 80% of Scala benefits with 20% of complexity

### Alternative 3: Groovy

**Pros**:

- Dynamic typing (faster prototyping)
- Concise syntax
- Good Spring Boot support (Grails)

**Cons**:

- Dynamic typing (loses compile-time safety)
- Slower runtime performance
- Less type safety
- Declining popularity

**Why not chosen**:

- We want type safety
- Kotlin gives us conciseness with type safety
- Groovy is less relevant today

### Alternative 4: Clojure

**Pros**:

- Functional programming (Lisp on JVM)
- Immutability by default
- Great concurrency primitives

**Cons**:

- Very different from Java/Kotlin
- Steep learning curve
- Smaller ecosystem
- Less Spring Boot integration
- Not taught in seminar

**Why not chosen**:

- Team not familiar with Lisp
- Harder to hire developers
- Less industry adoption for Spring Boot

---

## Consequences

### Positive

- **Null Safety**: Compile-time prevention of NullPointerException
- **Conciseness**: Less boilerplate, more readable code
- **Modern Features**: Functional programming, coroutines, sealed classes
- **Spring Boot Integration**: First-class Kotlin support in Spring
- **Axon Compatibility**: Excellent Axon Framework support
- **Developer Productivity**: Faster to write, easier to read
- **Learning**: Industry-relevant (Kotlin adoption growing rapidly)
- **Interoperability**: Can use any Java library seamlessly

### Negative

- **Compilation Speed**: Slightly slower than Java (but fast enough)
- **Learning Curve**: Developers familiar with Java need to learn Kotlin
    - **Mitigation**: Kotlin is easy to learn for Java developers (1-2 weeks)
- **Tooling**: IntelliJ IDEA recommended (but excellent Kotlin support)
- **Smaller Talent Pool**: Fewer Kotlin developers than Java
    - **Mitigation**: Java developers can learn Kotlin quickly

### Neutral

- **Java Interop**: Can call Java libraries, but sometimes requires platform types handling
    - **Note**: Minimal issue in practice, Kotlin handles Java interop well

---

## Implementation Standards

### Code Style

**Naming**:

- Classes/Interfaces: `PascalCase`
- Functions/Properties: `camelCase`
- Constants: `UPPER_SNAKE_CASE`
- Packages: lowercase

**Immutability**:

- Prefer `val` over `var`
- Use `data class` for DTOs and value objects
- Use immutable collections (`listOf`, not `mutableListOf`)

**Null Safety**:

- Avoid nullable types unless domain allows null
- Use `?` only when necessary
- Prefer `?: throw` over `!!`

**Functions**:

- Use expression body for simple functions: `fun double(x: Int) = x * 2`
- Use block body for complex functions
- Keep functions small (single responsibility)

**Example Service File**:

```kotlin
package org.pv293.kotlinseminar.paymentService.application.aggregates

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate
import org.pv293.kotlinseminar.paymentService.application.commands.impl.*
import org.pv293.kotlinseminar.paymentService.events.impl.*
import java.math.BigDecimal
import java.util.*

@Aggregate
class Payment() {
    @AggregateIdentifier
    private lateinit var paymentId: UUID
    private lateinit var orderId: UUID
    private lateinit var amount: BigDecimal
    private var status: PaymentStatus = PaymentStatus.CREATED

    @CommandHandler
    constructor(command: CreatePaymentCommand) : this() {
        AggregateLifecycle.apply(
            PaymentCreatedEvent(
                paymentId = command.paymentId,
                orderId = command.orderId,
                amount = command.amount
            )
        )
    }

    @EventSourcingHandler
    fun on(event: PaymentCreatedEvent) {
        this.paymentId = event.paymentId
        this.orderId = event.orderId
        this.amount = event.amount
        this.status = PaymentStatus.CREATED
    }
}

enum class PaymentStatus {
    CREATED, PROCESSING, PAID, RELEASED, FAILED
}
```

### Build Configuration

**Gradle Kotlin DSL**:

```kotlin
plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
    kotlin("plugin.jpa") version "1.9.20"
    id("org.springframework.boot") version "3.2.0"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

kotlin {
    jvmToolchain(21)
}
```

**Key Plugins**:

- `kotlin("plugin.spring")` - Makes classes open for Spring proxying
- `kotlin("plugin.jpa")` - Adds no-arg constructor for JPA entities
- `jackson-module-kotlin` - JSON serialization for Kotlin classes

---

## Metrics

**Lines of Code Reduction** (estimated vs. Java):

- Data classes: ~70% reduction
- Controllers: ~40% reduction
- Service classes: ~30% reduction
- Overall: ~35% less code

**Developer Productivity** (self-reported):

- Less time spent on boilerplate
- More time on business logic
- Better readability (easier code review)

**Null Safety Impact**:

- Zero NullPointerException in production (target)
- Compile-time null checks prevent runtime errors

---

## Adoption Strategy

### Learning Resources

1. **Official Kotlin Docs**: https://kotlinlang.org/docs/home.html
2. **Kotlin for Java Developers**: https://kotlinlang.org/docs/java-to-kotlin-idioms-strings.html
3. **Spring Boot + Kotlin**: https://spring.io/guides/tutorials/spring-boot-kotlin/
4. **Axon Framework Kotlin Examples**: https://github.com/AxonFramework/AxonFramework

### Interoperability with Java

- Can call Java libraries seamlessly
- Axon Framework is Java-based (works perfectly)
- Spring Framework supports Kotlin
- JPA/Hibernate work with Kotlin entities

**Example**:

```kotlin
// Calling Java library (Axon Framework)
AggregateLifecycle.apply(event)  // Java static method

// Spring annotations work
@RestController
@RequestMapping("/api/payment")
class PaymentController { ... }
```

---

## Compliance

This decision aligns with:

- Seminar teachings (Kotlin + Axon demonstrated in class)
- Modern JVM development practices
- Spring Boot best practices
- Industry trends (Kotlin adoption growing)

---

## References

- [Kotlin Official Documentation](https://kotlinlang.org/docs/home.html)
- [Spring Boot with Kotlin](https://spring.io/guides/tutorials/spring-boot-kotlin/)
- [Why Kotlin for Server-Side (JetBrains)](https://kotlinlang.org/lp/server-side/)
- [Axon Framework Kotlin Support](https://docs.axoniq.io/reference-guide/axon-framework/kotlin)
- [Kotlin for Spring Developers](https://www.baeldung.com/kotlin/spring-boot-kotlin)
- [Stack Overflow Survey 2024 - Kotlin](https://survey.stackoverflow.co/2024/#technology-most-loved-dreaded-and-wanted)
