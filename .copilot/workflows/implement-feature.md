# Feature Implementation Workflow

> **Purpose**
>
> This workflow defines the standard process for implementing a new feature in a production-grade Java Spring Boot microservice.
>
> The objective is to deliver a feature that is:
>
> - Correct
> - Secure
> - Testable
> - Performant
> - Maintainable
> - Production Ready

---

# Guiding Principles

Always

- Understand business requirements first
- Design before coding
- Follow architecture
- Write maintainable code
- Add tests
- Review thoroughly
- Keep changes small and incremental

Never

- Start coding without understanding requirements
- Skip design
- Break existing APIs
- Mix unrelated changes
- Ignore edge cases
- Ignore non-functional requirements

---

# Overall Workflow

```
Requirement
      │
      ▼
Requirement Analysis
      │
      ▼
Domain Modeling
      │
      ▼
API Design
      │
      ▼
Database Design
      │
      ▼
Implementation Plan
      │
      ▼
Implementation
      │
      ▼
Testing
      │
      ▼
Code Review
      │
      ▼
Deployment
      │
      ▼
Production Verification
```

---

# Step 1 — Understand Requirements

Collect

- Business objective
- Functional requirements
- Non-functional requirements
- User roles
- Acceptance criteria
- Constraints
- Dependencies
- Existing APIs
- Existing database

Clarify

- Happy path
- Failure scenarios
- Edge cases

Never assume missing requirements.

---

# Step 2 — Identify Domain Model

Identify

- Entities
- Value Objects
- Aggregates
- Relationships
- Ownership
- Lifecycle

Example

```
Booking

Seat

Show

Payment

Customer
```

---

# Step 3 — Validate Existing Architecture

Review

```
Controller

Service

Repository

Entity

Mapper

DTO

Security

Configuration
```

Determine

- What already exists
- What can be reused
- What must be extended

Avoid duplicate implementations.

---

# Step 4 — Design API

For every endpoint define

Method

```
GET

POST

PUT

PATCH

DELETE
```

URI

Example

```
POST /api/v1/bookings
```

Request DTO

Response DTO

Validation

HTTP Status

Error Responses

---

# Step 5 — Database Design

Determine

New tables

↓

New columns

↓

Indexes

↓

Constraints

↓

Relationships

Create

```
Flyway Migration
```

Review

- Foreign Keys
- Indexes
- Unique Constraints
- Cascade Rules

---

# Step 6 — Security Design

Determine

Authentication

Authorization

Ownership Validation

Permissions

Sensitive Data

Review

- Roles
- Access Control
- Audit Logging

---

# Step 7 — Define Implementation Plan

Break implementation into

```
Entity

Repository

DTO

Mapper

Service

Controller

Configuration

Migration

Tests
```

Implement one layer at a time.

---

# Step 8 — Create DTOs

Generate

```
CreateRequest

UpdateRequest

Response

SearchRequest
```

Add

Bean Validation

Do not expose entities.

---

# Step 9 — Implement Entity

Verify

- Relationships
- Indexes
- Constraints
- Versioning
- Audit Fields

Use

```
FetchType.LAZY
```

by default.

---

# Step 10 — Repository

Implement

- CRUD
- Query Methods
- Specifications
- Pagination
- EntityGraph (if required)

No business logic.

---

# Step 11 — Service

Implement

- Business Rules
- Validation
- Transactions
- External Calls
- Event Publishing

Keep service methods focused.

---

# Step 12 — Controller

Implement

- REST endpoints
- Validation
- DTO mapping
- Proper HTTP status codes

Controllers remain thin.

---

# Step 13 — Exception Handling

Verify

```
ResourceNotFoundException

ConflictException

BusinessException

ValidationException
```

Handled globally using

```
@RestControllerAdvice
```

---

# Step 14 — Logging

Log

- Important business events
- Resource IDs
- Correlation IDs

Never log

- Passwords
- Tokens
- Secrets

---

# Step 15 — Testing

Generate

## Unit Tests

Service

Mapper

Validator

## Repository Tests

Queries

Pagination

Relationships

## Controller Tests

Validation

Authentication

Responses

## Integration Tests

End-to-end request flow

---

# Step 16 — Performance Review

Review

- SQL queries
- Index usage
- N+1 queries
- Memory allocation
- Pagination
- Caching opportunities

Avoid unnecessary database calls.

---

# Step 17 — Security Review

Verify

✓ Authentication

✓ Authorization

✓ Validation

✓ Sensitive logging

✓ OWASP Top 10

✓ Proper error handling

---

# Step 18 — API Documentation

Update

OpenAPI / Swagger

Include

- Endpoint
- Request
- Response
- Errors
- Examples

---

# Step 19 — Code Review

Review

Architecture

↓

Naming

↓

SOLID

↓

Transactions

↓

Performance

↓

Security

↓

Tests

↓

Documentation

---

# Step 20 — Deployment

Deploy

```
DEV

↓

QA

↓

UAT

↓

Production
```

Monitor after deployment.

---

# Step 21 — Production Verification

Verify

- API success rate
- Error rate
- Response time
- Database metrics
- Logs
- Business KPIs

Confirm acceptance criteria.

---

# Layer Responsibilities

| Layer | Responsibility |
|---------|---------------|
| Controller | HTTP handling |
| Service | Business logic |
| Repository | Persistence |
| Entity | Persistence model |
| DTO | API contract |
| Mapper | Object mapping |
| Configuration | Framework setup |
| Security | Authentication & Authorization |

---

# Deliverables

A complete feature should include

```
Requirements

API Design

Database Migration

Entity

Repository

DTOs

Mapper

Service

Controller

Tests

Documentation
```

---

# AI Implementation Checklist

Before writing code verify

✓ Requirements understood

✓ Domain model identified

✓ Existing architecture reviewed

✓ API designed

✓ Database changes planned

✓ Security considered

✓ Validation defined

✓ Transactions planned

✓ Tests identified

✓ Documentation updated

---

# AI Coding Order

Generate in this order

```
1. Flyway Migration

2. Entity

3. Repository

4. DTOs

5. Mapper

6. Service Interface

7. Service Implementation

8. Controller

9. Exception Handling (if required)

10. Tests

11. OpenAPI Documentation
```

Never generate layers in random order.

---

# Pull Request Checklist

Include

- Feature summary
- Business requirement
- API changes
- Database changes
- Backward compatibility
- Risks
- Screenshots (if UI)
- Test evidence

---

# AI Output Format

## Requirement Summary

```
...
```

## Domain Model

```
Entities

Relationships
```

## API Changes

```
Endpoints

Request DTOs

Response DTOs
```

## Database Changes

```
Tables

Columns

Indexes

Constraints
```

## Files to Create

```
Entity

Repository

DTO

Mapper

Service

Controller

Migration

Tests
```

## Files to Modify

```
...
```

## Testing Strategy

```
Unit Tests

Integration Tests

Repository Tests

Controller Tests
```

## Risks

```
Low

Medium

High
```

## Validation Checklist

```
✓ Business rules implemented

✓ Validation complete

✓ Transactions correct

✓ Security reviewed

✓ Tests passing

✓ Documentation updated

✓ Ready for Production
```

---

# Definition of Done

A feature is complete only when

- Requirements satisfied
- Acceptance criteria met
- API implemented
- Database migrated
- Business rules implemented
- Validation complete
- Security reviewed
- Performance reviewed
- Tests written and passing
- Documentation updated
- Code reviewed
- Successfully deployed
- Production verification completed