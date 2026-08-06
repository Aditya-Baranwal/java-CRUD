# Microservice Development Skill

> **Skill Name**
>
> Production-Grade Java Microservice Development

---

# Purpose

This skill defines **how an AI agent should design, implement, review, and improve a Spring Boot microservice**.

The agent should think like a **Senior/Staff Backend Engineer**, balancing

- Maintainability
- Scalability
- Performance
- Reliability
- Security
- Simplicity

The goal is **production-ready software**, not demo code.

---

# Technology Stack

Default assumptions

```
Java 21
Spring Boot 3.x
Spring MVC
Spring Data JPA
Spring Security
PostgreSQL
Flyway
Maven
JUnit 5
Mockito
Docker
OpenAPI
SLF4J
```

If the project already uses another technology,
follow the existing stack.

---

# Architecture

Always follow

```
Controller
      ↓
Service
      ↓
Repository
      ↓
Database
```

Additional layers

```
Controller

↓

DTO

↓

Mapper

↓

Service

↓

Repository

↓

Entity
```

Never bypass layers.

---

# Project Structure

```
src/main/java

controller/

service/

repository/

entity/

dto/

mapper/

validator/

config/

exception/

security/

event/

client/

scheduler/

util/
```

Large projects should use

Feature-first packaging.

---

# Development Workflow

Before generating code

1. Read project context
2. Read API specification
3. Read database schema
4. Read business rules
5. Search for existing implementation
6. Reuse existing classes whenever possible

Never create duplicate functionality.

---

# Controller Rules

Controllers should

- Receive HTTP requests
- Validate input
- Call service
- Return DTOs

Controllers must NOT

- Execute business logic
- Call repositories
- Publish events
- Perform calculations

---

# Service Rules

Services should

- Implement business logic
- Coordinate repositories
- Manage transactions
- Publish domain events
- Call external systems

Services must NOT

- Build HTTP responses
- Write SQL
- Perform manual DTO mapping

---

# Repository Rules

Repositories should

- Perform persistence
- Execute queries
- Support pagination
- Support specifications

Repositories must NOT

- Implement business rules
- Publish events
- Call services

---

# Entity Rules

Entities represent database state.

Entities must NOT

- Contain business logic
- Access repositories
- Call services
- Return API responses

Prefer

```
LAZY
```

relationships.

---

# DTO Rules

Always separate

```
Request DTO

Response DTO
```

Never expose entities directly.

---

# Mapper Rules

Use

```
MapStruct
```

when available.

Mapper responsibilities

```
DTO

↓

Entity

↓

Response
```

No business logic.

---

# Validation

Validation flow

```
Bean Validation

↓

Business Validation

↓

Persistence
```

Business validation belongs inside Service.

---

# Transactions

Transaction ownership

```
Service Layer
```

Read

```
@Transactional(readOnly = true)
```

Write

```
@Transactional
```

---

# Exception Handling

Throw domain exceptions

Examples

```
BusinessException

ConflictException

ValidationException

ResourceNotFoundException
```

Global exception handler converts exceptions to HTTP responses.

---

# Logging

Use structured logging.

Log

- Resource ID
- User ID
- Action
- Duration

Never log

- Passwords
- Tokens
- Secrets
- PII

---

# REST Guidelines

Use REST conventions.

Good

```
POST /courses

GET /courses/{id}

PUT /courses/{id}

DELETE /courses/{id}
```

Avoid

```
/createCourse

/updateCourse
```

---

# Security

Always verify

- Authentication
- Authorization
- Ownership

Never trust client input.

Validate everything.

---

# Persistence

Prefer

```
existsBy()

countBy()

Pageable

EntityGraph

Specifications
```

Avoid

```
SELECT *

N+1 Queries
```

---

# Performance

Always evaluate

- Database calls
- Network calls
- Object allocation
- Memory usage

Prefer

- Batch processing
- Caching (when applicable)
- Pagination
- Streaming large datasets

---

# Concurrency

Prefer

```
Optimistic Locking
```

Use

```
@Version
```

Avoid synchronization unless required.

---

# API Design

Every API should define

- Request DTO
- Response DTO
- Status codes
- Validation
- Error responses
- Pagination
- Filtering
- Sorting

---

# Event-Driven Design

Publish events only after successful transactions.

Examples

```
UserCreatedEvent

OrderPlacedEvent

PaymentCompletedEvent
```

Do not publish events before commit.

---

# External Service Calls

Whenever calling another service

Apply

- Timeout
- Retry
- Circuit Breaker (if available)
- Fallback (when appropriate)

Handle failures gracefully.

---

# Database Design

Prefer

- Normalized schema
- Foreign keys
- Audit fields
- Version column

Avoid unnecessary denormalization.

---

# Testing Strategy

Generate

### Unit Tests

- Business logic
- Validation
- Exceptions

### Integration Tests

- Database
- Transactions
- Repository

### Controller Tests

- Validation
- Security
- Response

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

Avoid

- Duplicate logic
- Large classes
- Utility dumping grounds

---

# Design Patterns

Use only when beneficial.

Preferred

- Strategy
- Factory
- Builder
- Observer
- Decorator
- Adapter
- Template Method
- State

---

# Documentation

Whenever APIs change

Update

```
docs/api.md
```

Whenever schema changes

Update

```
docs/database.md
```

Whenever business rules change

Update

```
context/business-rules.md
```

---

# AI Review Checklist

Before returning code verify

✓ Architecture respected

✓ No business logic in controller

✓ Repository only performs persistence

✓ DTO separation maintained

✓ Transactions correct

✓ Validation complete

✓ Exception handling correct

✓ Logging appropriate

✓ Security respected

✓ No duplicate logic

✓ Performance acceptable

✓ Production-ready

---

# Anti-Patterns to Avoid

Never generate

- God classes
- Fat controllers
- Anemic services
- Repository business logic
- Static utility business methods
- Circular dependencies
- Field injection
- `SELECT *`
- Manual entity mapping (when mapper exists)
- Hardcoded configuration
- Silent exception swallowing

---

# Definition of Done

A feature is complete only when

- Code compiles
- Tests pass
- Business rules implemented
- Validation complete
- Transactions correct
- Logging added
- Security reviewed
- Documentation updated
- Performance considered
- No architectural violations
- Ready for production

---

# Invocation Examples

### Generate a Feature

```
Implement Course Management module.
```

The AI should generate

- Entity
- Repository
- Service
- Controller
- DTOs
- Mapper
- Validation
- Tests

following project standards.

---

### Review Existing Code

```
Review BookingService.java
```

The AI should

- Detect bugs
- Find architecture violations
- Suggest improvements
- Preserve behavior
- Follow project coding guidelines

---

### Refactor Code

```
Refactor UserController.java
```

The AI should

- Keep behavior unchanged
- Improve readability
- Remove duplication
- Follow project architecture
- Preserve API contracts

---

### Implement an API

```
Add GET /courses/{id}
```

The AI should

- Update `docs/api.md`
- Generate Controller
- Generate Service
- Generate Repository method (if required)
- Generate DTOs
- Generate Mapper changes
- Generate Tests
- Follow all project conventions