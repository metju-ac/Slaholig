# AGENTS.md

This repo is a multi-module Gradle (Kotlin) monorepo with several Spring Boot microservices
and a shared Kotlin library.

Event-driven architecture uses Axon Framework; primary docs: https://docs.axoniq.io/home/

No Cursor rules were found in `.cursor/rules/` or `.cursorrules`.
No Copilot rules were found in `.github/copilot-instructions.md`.

## Repo Layout

- `shared/`: shared DTOs, events, queries, and common exceptions
- `courses-service/`, `enrollment-service/`, `forum-service/`, `assignment-service/`, `product-selection-service/`, `payment-service/`: Spring Boot services
- `docker-compose.yml`: local stack (Axon Server, Postgres per service, Grafana/Loki/Prometheus)

## Prereqs

- JDK 21 (project uses `kotlin { jvmToolchain(21) }`)
- Use the Gradle wrapper: `./gradlew`

## Build / Lint / Test Commands

Note: there is no dedicated linter/formatter task configured (no ktlint/spotless/detekt).
Use `check` and compilation errors as the main signal, plus tests.

### Common (root)

- Build everything: `./gradlew build`
- Run all verification: `./gradlew check`
- Compile fast (no tests): `./gradlew assemble`
- Clean: `./gradlew clean`
- List modules: `./gradlew projects`

### Per-module

- Build one service: `./gradlew :courses-service:build`
- Test one service: `./gradlew :courses-service:test`
- Check one service: `./gradlew :courses-service:check`

### Run services

- Full local stack: `docker compose up`
- Run a single service (from source): `./gradlew :courses-service:bootRun`

### Running a single test (JUnit 5)

Gradle test filtering works even if the repo currently has few/no tests.

- One test class:
  `./gradlew :courses-service:test --tests "org.pv293.kotlinseminar.SomeTest"`
- One test method:
  `./gradlew :courses-service:test --tests "org.pv293.kotlinseminar.SomeTest.someCase"`

Tips:
- Add `--info` for more output.
- Add `--rerun-tasks` if you suspect stale results.
- Prefer module-scoped runs (`:module:test`) to keep feedback tight.
- For faster local iteration: `./gradlew :courses-service:test --tests "..." --no-daemon`.

### Useful Gradle flags

- Refresh deps: `./gradlew build --refresh-dependencies`
- Configure/diagnose: `./gradlew -s check` (stacktraces) or `./gradlew --scan check`
- Run just compilation: `./gradlew :courses-service:compileKotlin :courses-service:compileTestKotlin`

## Service Ports / Local Docs

When running via Docker Compose:

- Enrollment: http://localhost:8080/swagger-ui.html
- Courses: http://localhost:8081/swagger-ui.html
- Forum: http://localhost:8082/swagger-ui.html
- Assignment: http://localhost:8083/swagger-ui.html
- Product Selection: http://localhost:8084/swagger-ui.html
- Payment: http://localhost:8085/swagger-ui.html

## Code Style Guidelines (Kotlin / Spring Boot)

### Imports

- Prefer explicit imports.
- Wildcard imports are acceptable only for annotation-heavy Spring MVC usage
  (e.g. `org.springframework.web.bind.annotation.*`) if it matches the file's current style.
- Keep imports grouped: Kotlin/JDK, third-party, project (default IDE behavior is fine).

### Formatting

- 4-space indentation.
- Use trailing commas in multiline argument lists/constructors (already used in several files).
- Keep functions small; split long parameter lists across lines.
- Use string templates for logs (already common): `logger.info("... $value")`.

### Types and nullability

- Avoid platform types; keep Kotlin types explicit at API boundaries.
- Prefer non-null types; use nullable types only when the domain truly allows missing values.
- Use `UUID` as the internal identifier type; parse external strings at the boundary.

### Naming

- Packages: lowerCamel segments following current convention
  (e.g. `org.pv293.kotlinseminar.coursesService`).
- Classes: `PascalCase`.
- Functions/variables: `lowerCamelCase`.
- DTOs: `*DTO` suffix (matches existing code).
- Commands/Events/Queries: suffix with `Command`, `Event`, `Query`.

### API / Controller patterns

- Controllers are thin: log, validate/parse inputs, call Axon gateways, return DTOs.
- Prefer `UUID` in application code; controllers may accept `String` path params and convert.
- Keep endpoint paths consistent with existing controllers (`/courses`, `/enrollment`, etc.).
- Prefer returning domain DTOs, not entities.

### Error handling

- For HTTP APIs, throw `ResponseStatusException` for request failures.
- For domain lookups, `shared` defines `NotFoundException`; map it to 404 at the boundary.
- Don't swallow exceptions; log with context (ids, key fields) and rethrow.
- Avoid catching broad `Exception` unless you translate to a specific HTTP response.

### Logging

- Use SLF4J: `private val logger = LoggerFactory.getLogger(X::class.java)`.
- Log at `info` for normal workflow, `error` for failures with actionable context.
- Do not log secrets/credentials (DB passwords are in compose for local use; keep them local).

### Spring / Kotlin best practices

- Prefer constructor injection (already used).
- Keep Spring configuration in `infrastructure/` packages.
- Avoid field injection (`@Autowired`) in new code.
- Keep transactional boundaries explicit if/when adding persistence logic.
- Prefer `val` over `var`; use `data class` for DTOs and immutable value objects.

### Runtime profiles

- Docker Compose runs services with `spring_profiles_active: prod`.
- When running from source, default to the `dev` profile if you need local DB/H2 tweaks:
  `./gradlew :courses-service:bootRun --args='--spring.profiles.active=dev'`

### Shared module usage

- Put cross-service contracts in `shared/`: DTOs, events, queries, common exceptions.
- Keep `shared` dependency direction one-way: services depend on `shared`, not vice versa.
- When changing contracts in `shared`, update all services that consume them.

## Agent Workflow Expectations

- Before coding, identify the target module (`:courses-service`, etc.).
- Prefer module-scoped builds/tests for iteration; run `./gradlew build` before finalizing.
- If adding tests, place them under `src/test/kotlin` and use JUnit 5.
