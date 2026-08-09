# Engineering Standards

This document defines the engineering standards for the Learning Management System (LMS).

It establishes coding conventions, development practices, and implementation rules to ensure consistency across the codebase.

Detailed functional specifications are available under the `docs/` directory.

---

# Source of Truth

For detailed specifications refer to

- docs/api.md
- docs/database.md
- docs/schema.md
- docs/security.md
- docs/architecture.md

If conflicts exist, documents under `docs/` take precedence.

---

# General Principles

Follow

- SOLID
- DRY
- KISS
- YAGNI
- Composition over Inheritance
- Separation of Concerns

Code should prioritize

- Readability
- Simplicity
- Testability
- Maintainability

---

# Java Standards

Use

- Java 21+
- Records for immutable DTOs where appropriate
- Enum instead of constant classes
- Optional only as a return type
- Constructor Injection, using @RequiredArgsConstructor for services and controllers
- use `final` for fields where possible
- use Lombok for boilerplate reduction
- use only required imports, avoid wildcard imports

Avoid

- Field Injection
- Static mutable state
- Public mutable fields
- Raw types
- Magic numbers

---

# Spring Boot Standards

Use

- Spring Boot
- Spring Data JPA
- Bean Validation
- Spring Security
- Spring Redisson

Avoid

- Business logic in Controllers
- Manual bean creation
- Circular dependencies

---

# Package Structure

```
controller/
service/
repository/
entity/
enums/
dto/
mapper/
config/
security/
exception/
validation/
util/
```

New packages should only be introduced when justified.

---

# Controller Standards

Controllers

- expose REST endpoints
- validate requests
- invoke services
- return DTOs

Controllers must never

- access repositories
- implement business logic
- perform transactions
- return entities

---

# Service Standards

Services

- contain business logic
- define transaction boundaries
- coordinate repositories
- manage cache updates

Services must never

- depend on controllers
- expose entities directly
- contain HTTP-specific logic

---

# Repository Standards

Repositories

- own persistence
- contain query methods
- use Spring Data JPA

Repositories must never

- implement business rules
- call controllers
- call services

Prefer

- Derived Queries
- JPQL

Use Native SQL only when necessary.

---

# Entity Standards

Entities

- represent one database tables
- use JPA annotations
- default relationships to LAZY loading
- match schema exactly

Entities must not

- contain controller logic
- expose API contracts
- perform validation

---

# DTO Standards

Separate

- Request DTO, should be created through open-api
- Response DTO, should be created through open api

Never

- expose entities through APIs
- reuse entities as request models

DTO naming

```
CreateCourseRequest

UpdateCourseRequest

CourseResponse
```

---

# Mapper Standards

All object conversion belongs in mapper classes.

Responsibilities

- Entity → DTO
- DTO → Entity

Do not mix mapping with business logic.

---

# Validation Standards

Use Jakarta Bean Validation.

Examples

- @NotNull
- @NotBlank
- @Size
- @Email
- @Pattern

Business validation belongs in the validation layer.

---

# Exception Handling

Use

- Custom Exceptions
- Global Exception Handler
- Standard Error Response
- stack trace can be exposed in dev, qa, local

Do not

- expose stack traces on prod
- catch Exception unnecessarily

---

# Logging Standards

Use structured logging.

Log

- Request ID
- User ID
- Endpoint
- HTTP Method
- Status
- Execution Time

Never log

- Passwords
- JWT Tokens
- Secrets
- Sensitive personal information

---

# REST Standards

Follow docs/api.md for detailed API specifications.

We use OpenAPI 3.0 for API documentation.

DTOs, Controller must align with API documentation in generated file `resources/openapi.yaml`.

# API Response Standards

Every response follows

```json
{
    "message": "",
    "data": {},
    "timestamp": ""
}
```

Errors follow

```json
{
    "message": "",
    "errorCode": "",
    "errors": []
}
```

---

# Database Standards

Primary Keys

- BIGINT
- Auto Increment

Soft Delete

- is_active

Audit Columns

- created_at
- created_by
- updated_at
- updated_by

Repositories own all database access.

---

# Transaction Standards

Transactions belong only in services.

Preferred

```
@Transactional
```

Avoid transaction management inside repositories.

---

# Caching Standards

Redis is the approved cache.

Cache

- Course
- Module
- Lesson

Do not cache

- Enrollment
- Progress
- Authentication state

Evict cache after successful updates.

---

# Security Standards

Authentication

- JWT

Authorization

- Role Based Access Control

Passwords

- Bcrypt

Never

- store plaintext passwords
- trust client-provided roles

---

# Configuration Standards

Externalize

- Database URL
- Redis URL
- JWT Secret
- Server Port

Never hardcode environment-specific values.

---

# Naming Conventions

Classes

```
CourseController

CourseService

CourseRepository

CourseMapper
```

Interfaces

```
PricingStrategy

NotificationServiceInterface

CacheServiceInterface
```

DTOs

```
CreateCourseRequest

UpdateLessonRequest

EnrollmentResponse
```

Exceptions

```
CourseNotFoundException

DuplicateEnrollmentException
```

Enums

```
UserRole

LessonStatus

CourseStatus
```

---

# Testing Standards

Every business feature should have

- Unit Tests
- Service Tests
- Repository Tests (where applicable)
- Controller Tests

Mock external dependencies.

Business logic should be independently testable.

---

# Performance Guidelines

Prefer

- Pagination
- Batch fetching
- Proper indexing
- Redis caching

Avoid

- N+1 queries
- Eager loading
- Unbounded result sets

---

# Code Review Checklist

Before merging code verify

- Controller contains no business logic.
- Service owns business rules.
- Repository only accesses the database.
- DTOs are used for API communication.
- Entities are not exposed externally.
- Validation is implemented.
- Transactions exist only in services.
- Logging follows standards.
- Exceptions use standard responses.
- Security rules are enforced.
- Cache is updated correctly.
- Tests are included.
- No duplicated logic.
- Naming conventions are followed.

---

# AI Context Usage

This document defines **how code should be written**.

For business rules

```
context/business-domain.md
```

For architectural constraints

```
context/architectural-spec.md
```

For implementation guidance

```
prompts/
```

For detailed specifications

```
docs/
```