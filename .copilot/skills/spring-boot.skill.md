# Spring Boot Development Skill

> **Skill Name**
>
> Production-Grade Spring Boot Development

---

# Purpose

This skill defines how an AI agent should design, implement, review, and maintain Spring Boot applications.

The AI should generate code that is

- Production-ready
- Maintainable
- Testable
- Performant
- Secure
- Observable

Follow Spring Boot best practices instead of simply making code compile.

---

# Spring Boot Philosophy

Always follow

- Convention over Configuration
- Dependency Injection
- Loose Coupling
- Single Responsibility
- Auto Configuration
- Externalized Configuration

Never fight Spring Boot conventions without a valid reason.

---

# Project Structure

```
src/main/java

config/

controller/

service/

repository/

entity/

dto/

mapper/

validator/

security/

exception/

client/

event/

scheduler/

util/
```

Large applications should use

```
feature-first packaging
```

---

# Bean Management

Prefer Spring-managed beans.

Use

```
@Component

@Service

@Repository

@Controller

@RestController

@Configuration
```

Only create beans when necessary.

Avoid unnecessary components.

---

# Dependency Injection

Always use

Constructor Injection.

Example

```java
@RequiredArgsConstructor
@Service
public class CourseService {

    private final CourseRepository repository;

}
```

Never use

```
@Autowired
```

field injection.

---

# Configuration

Externalize configuration.

Use

```
application.yml

application-<profile>.yml
```

Inject using

```
@ConfigurationProperties
```

Prefer

```
@ConfigurationProperties
```

over excessive

```
@Value
```

usage.

Never hardcode

- URLs
- Secrets
- Ports
- Timeouts

---

# Profiles

Support environments using

```
local

dev

qa

uat

prod
```

Never hardcode environment-specific behavior.

---

# REST Controllers

Controllers should

- Receive requests
- Validate input
- Call service
- Return DTOs

Controllers must NOT

- Contain business logic
- Access repositories
- Execute SQL
- Publish events

---

# Service Layer

Services should

- Implement business rules
- Manage transactions
- Coordinate repositories
- Publish events
- Call external systems

Services must NOT

- Return ResponseEntity
- Access HTTP objects
- Write SQL

---

# Repository Layer

Repositories should

- Extend JpaRepository
- Use Specifications where appropriate
- Support pagination
- Execute persistence only

Avoid business logic.

---

# DTO Strategy

Separate

```
Request DTO

Response DTO
```

Never expose entities directly.

---

# Entity Design

Entities represent persistence state.

Avoid business logic inside entities.

---

# Validation

Use Bean Validation.

Examples

```
@NotNull

@NotBlank

@Positive

@Email

@Size
```

Business validation belongs in Service.

---

# Transactions

Service owns transactions.

Write

```
@Transactional
```

Read

```
@Transactional(readOnly = true)
```

Avoid transactions inside controllers.

---

# Exception Handling

Use

```
@RestControllerAdvice
```

Create domain exceptions

```
BusinessException

ConflictException

ValidationException

ResourceNotFoundException
```

Never catch

```
Exception
```

unless necessary.

---

# Logging

Use

```
SLF4J
```

Log

- Resource ID
- User ID
- Correlation ID
- Duration

Never log

- Passwords
- JWTs
- Secrets
- Sensitive PII

---

# Security

Integrate with

```
Spring Security
```

Support

- Authentication
- Authorization
- Method Security

Use

```
@PreAuthorize
```

when appropriate.

Never disable security for convenience.

---

# Configuration Properties

Prefer

```
@ConfigurationProperties(prefix = "mail")
```

instead of scattered

```
@Value
```

properties.

Validate configuration where appropriate.

---

# Scheduling

Use

```
@EnableScheduling

@Scheduled
```

for scheduled jobs.

Keep scheduled methods

- Idempotent
- Short-lived
- Retry-safe

---

# Async Processing

Use

```
@Async
```

only when appropriate.

Configure dedicated executors.

Never rely on default thread pools in production.

---

# Caching

Use Spring Cache abstraction or Redisson as mentioned in context files

Examples

```
@Cacheable

@CachePut

@CacheEvict
```

Cache only read-heavy, stable data.

---

# REST Client

Prefer

```
RestClient
```

(Spring Boot 3.2+)

or project-approved client.

Configure

- Timeout
- Retry
- Error handling

Avoid unmanaged HTTP clients.

---

# Spring Data JPA

Prefer

- Derived queries
- Specifications
- Pageable
- EntityGraph

Avoid

- N+1 queries
- Native SQL unless necessary

---

# Bean Lifecycle

Understand and use

```
@PostConstruct

@PreDestroy
```

sparingly.

Avoid heavy initialization during startup.

---

# Auto Configuration

Leverage Spring Boot auto-configuration.

Avoid overriding defaults unless necessary.

---

# Actuator

Enable

```
Spring Boot Actuator
```

Expose only required endpoints.

Typical production endpoints

```
health

info

metrics

prometheus
```

Protect sensitive endpoints.

---

# Observability

Support

- Health Checks
- Metrics
- Distributed Tracing
- Correlation IDs

Prefer

```
Micrometer
```

for metrics.

---

# Testing

Generate

### Unit Tests

- Mockito
- JUnit 5

### Controller Tests

```
@WebMvcTest
```

### Repository Tests

```
@DataJpaTest
```

### Integration Tests

```
@SpringBootTest
```

Use Testcontainers when available.

---

# Performance

Optimize

- Startup time
- Bean creation
- Database access
- Memory usage
- Object allocation

Avoid

- Blocking operations
- Excessive reflection
- Heavy initialization

---

# Code Quality

Follow

- SOLID
- DRY
- KISS
- YAGNI

Method target

```
<30 lines
```

Class target

```
Single Responsibility
```

---

# Common Spring Boot Anti-Patterns

Never generate

- Field Injection
- Fat Controllers
- Business Logic in Repository
- Manual Bean Creation without reason
- Static Spring Beans
- Circular Dependencies
- Exposed Entities
- Catching generic Exception
- Hardcoded Configuration
- Manual Object Mapping when mapper exists

---

# Component Scanning

Spring Boot automatically discovers Spring beans using **component scanning**.

Common annotations:

* `@Component`
* `@Service`
* `@Repository`
* `@Controller`
* `@RestController`

`@SpringBootApplication` includes `@ComponentScan` and scans the package containing the application class and its subpackages.

```java
@SpringBootApplication
public class Application {
}
```

Example:

```java
@Service
public class UserService {
}
```

`UserService` is automatically registered as a Spring bean if it is within the component-scan scope.

**Tip:** Keep the main application class in the root package so Spring can discover all application components.

# AI Review Checklist

Before returning code verify

✓ Constructor Injection

✓ Bean Validation

✓ Transaction boundaries correct

✓ DTO separation maintained

✓ Repository used correctly

✓ REST conventions followed

✓ Exception handling centralized

✓ Security respected

✓ Logging appropriate

✓ Configuration externalized

✓ Tests considered

✓ Production-ready

---

# Definition of Done

A Spring Boot feature is complete only when

- Application starts successfully
- Dependency Injection works correctly
- Validation implemented
- Transactions managed
- Security configured
- Logging added
- Configuration externalized
- Tests written
- Documentation updated
- Performance considered
- Ready for production

---

### Review Spring Boot Code

```
Review BookingController.java
```

The AI should verify

- Spring conventions
- Layer separation
- Validation
- Transactions
- Security
- Dependency Injection
- Performance
- Production readiness