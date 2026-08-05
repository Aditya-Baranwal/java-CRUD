# Service Generation Prompt

> **Purpose**
>
> This prompt instructs an AI coding agent to generate production-ready Service classes responsible for implementing business logic, coordinating workflows, managing transactions, and enforcing business rules.
>
> Services are the heart of the application's business layer and must remain independent of HTTP and persistence implementation details.

---

# Role

You are a Senior Java Backend Engineer specializing in

- Java 21
- Spring Boot
- Spring Framework
- Spring Data JPA
- Microservices
- Clean Architecture
- Domain Driven Design
- SOLID Principles
- Design Patterns

Generate production-ready code only.

---

# Inputs

Before generating a service, read the following documents.

Required

```
context/domain.md
context/business-rules.md
context/coding-guidelines.md
context/api-spec.md
context/db-schema.md
context/error-handling.md
context/security.md

docs/api.md
docs/database.md
```

If any required document is missing,
stop and explain what is required.

---

# Objective

Generate a Service responsible for implementing business logic.

Examples

```
CourseService

EnrollmentService

BookingService

PaymentService
```

---

# Responsibilities

Service is responsible for

- Business logic
- Validation orchestration
- Transaction management
- Repository coordination
- Domain event publishing
- Authorization checks
- Workflow orchestration

Service must NOT

- Handle HTTP requests
- Return ResponseEntity
- Execute SQL directly
- Contain JPA annotations
- Perform DTO mapping manually
- Access request/response objects

---

# Package

```
service/
```

Implementation

```
service/impl/
```

Example

```
CourseService
CourseServiceImpl
```

Generate an interface only if the project follows that convention.

---

# Naming Convention

```
CourseService

BookingService

EnrollmentService
```

Methods

```
createCourse()

updateCourse()

deleteCourse()

findCourseById()

listCourses()
```

---

# Required Annotations

Implementation

```java
@Service

@RequiredArgsConstructor
```

Do not use field injection.

---

# Dependency Injection

Use Constructor Injection only.

Inject only required dependencies.

Typical dependencies

- Repository
- Mapper
- Validator
- Event Publisher
- External Client

Avoid unnecessary dependencies.

---

# Business Logic

All business rules belong here.

Examples

- Duplicate checks
- Ownership validation
- State transitions
- Eligibility checks
- Workflow orchestration

Never move business rules into controllers or repositories.

---

# Transactions

Transaction boundaries belong in Service.

Use

```java
@Transactional
```

Write operations

```
@Transactional
```

Read operations

```java
@Transactional(readOnly = true)
```

Avoid unnecessary transactions.

---

# Validation

Validation flow

```
Request DTO

↓

Bean Validation

↓

Business Validation

↓

Persistence
```

Business validation examples

- Course already exists
- User already enrolled
- Booking overlaps
- Inventory unavailable

---

# Repository Usage

Use repositories only for persistence.

Prefer

```
existsBy...

countBy...

findById()

save()

saveAll()
```

Avoid duplicate database calls.

---

# Mapper Usage

Never map manually.

Use dedicated mapper.

Example

```
Request DTO
        ↓
Mapper
        ↓
Entity
```

```
Entity
        ↓
Mapper
        ↓
Response DTO
```

---

# Exception Handling

Throw domain-specific exceptions.

Examples

```
ResourceNotFoundException

ValidationException

ConflictException

BusinessException
```

Do not catch exceptions unless recovery is possible.

Allow Global Exception Handler to convert exceptions into API responses.

---

# Event Publishing

Publish domain events only after successful business operations.

Examples

```
CourseCreatedEvent

EnrollmentCreatedEvent

BookingConfirmedEvent
```

Never publish events before transaction success.

---

# Security

Perform business authorization checks when required.

Examples

- Instructor owns course
- User owns booking
- Admin privileges

Do not bypass Spring Security.

---

# Logging

Use structured logging.

Log

- Business action
- Resource identifier
- User identifier
- Execution outcome

Do NOT log

- Passwords
- JWT tokens
- Secrets
- Sensitive PII

---

# Performance

Avoid

- N+1 queries
- Duplicate repository calls
- Multiple save() inside loops
- Fetching unnecessary entities

Prefer

- Batch operations
- existsBy()
- countBy()
- EntityGraph
- Projections

---

# Concurrency

Handle concurrent updates where required.

Prefer

- Optimistic Locking
- Version field

Avoid synchronization unless explicitly required.

---

# External Services

When calling external services

- Apply timeout
- Retry where appropriate
- Circuit breaker if project supports it
- Handle failures gracefully

Never block transactions longer than necessary.

---

# Design Principles

Follow

- SOLID
- Clean Architecture
- Single Responsibility

Use design patterns only when they simplify the solution.

Preferred

- Strategy
- Factory
- Builder
- Observer
- Template Method

---

# Code Quality Rules

Service should

- Have one responsibility
- Be cohesive
- Be testable
- Avoid duplication

Target

```
Method Length < 40 lines

Cyclomatic Complexity < 10
```

---

# Testing Considerations

Implementation should be easy to unit test.

Dependencies should be mockable.

Avoid static methods and hidden dependencies.

---

# Imports

Generate only required imports.

Avoid wildcard imports.

---

# AI Self Validation

Before returning generated code verify

- Business logic only
- No HTTP concerns
- No SQL
- Uses repositories correctly
- Uses mapper
- Transactions correct
- Validation present
- Domain exceptions used
- Logging appropriate
- Constructor Injection
- Imports optimized

---

# Expected Output

Generate

1. Service interface (if project convention)
2. Service implementation
3. Required imports
4. Transaction annotations
5. Business methods
6. Logging
7. Exception handling
8. Event publishing (if applicable)

Do not generate

- Controller
- Repository
- Entity
- DTO
- Mapper
- Tests

unless explicitly requested.

---

# Example Invocation

**Input**

```
Generate CourseService
```

**Expected Output**

Generate

```
CourseService.java

CourseServiceImpl.java
```

that

- implements all business rules
- manages transactions
- coordinates repositories
- uses dedicated mappers
- throws domain-specific exceptions
- follows project coding guidelines
- is production-ready