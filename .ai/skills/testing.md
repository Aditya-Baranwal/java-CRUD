# Testing Skill

> **Skill Name**
>
> Production-Grade Testing for Java Spring Boot Applications

---

# Purpose

This skill defines how an AI agent should design, generate, review, and improve automated tests for a production-grade Java Spring Boot application.

Testing should validate

- Functional correctness
- Business rules
- Regression safety
- Integration behavior
- Security
- Performance assumptions

Tests should increase confidence while remaining maintainable.

---

# Technology Stack

Default assumptions

```
Java 21
JUnit 5
Mockito
AssertJ
Spring Boot Test
MockMvc
Spring Security Test
Testcontainers
REST Assured
Maven
```

Use the project's existing testing framework when different.

---

# Testing Philosophy

Follow the Testing Pyramid.

```
                E2E

          Integration Tests

              Unit Tests
```

Prefer

- Many Unit Tests
- Sufficient Integration Tests
- Few End-to-End Tests

---

# Test Objectives

Every feature should verify

- Happy Path
- Business Rules
- Invalid Inputs
- Edge Cases
- Error Handling
- Authorization
- Transactions
- Persistence

---

# Test Organization

Mirror production package structure.

```
src/test/java

controller/

service/

repository/

mapper/

security/

integration/

util/

fixture/
```

---

# Naming Convention

Classes

```
BookingServiceTest

CourseControllerTest

UserRepositoryTest
```

Methods

```
shouldCreateBooking()

shouldRejectDuplicateBooking()

shouldReturn404WhenBookingMissing()

shouldRollbackTransactionOnFailure()
```

Method names should describe expected behavior.

---

# Unit Tests

Use

```
JUnit 5

Mockito

AssertJ
```

Mock all external collaborators.

Never mock

- DTOs
- Entities
- Value Objects

Do not load Spring Context.

---

# Service Tests

Mock

- Repository
- Mapper
- Validator
- Event Publisher
- External Clients

Verify

- Business logic
- Validation
- Repository interaction
- Exceptions
- Events

---

# Controller Tests

Use

```
@WebMvcTest
```

Mock

Service Layer

Verify

- HTTP Status
- Validation
- JSON Response
- Error Response
- Authentication
- Authorization

Never hit the real database.

---

# Repository Tests

Use

```
@DataJpaTest
```

Verify

- Query correctness
- Pagination
- Sorting
- Constraints
- Relationships

Prefer Testcontainers when supported.

---

# Integration Tests

Use

```
@SpringBootTest
```

Verify

- Full request flow
- Transactions
- Database
- Event publishing
- Security integration

Avoid mocking application components.

---

# Security Tests

Verify

- Authentication required
- Authorization rules
- Invalid JWT
- Missing role
- Ownership validation
- Anonymous access

---

# Validation Tests

Generate tests for

- Required fields
- Invalid values
- Invalid enum
- Maximum length
- Minimum length
- Boundary values

Every validation rule should have a test.

---

# Exception Tests

Verify

```
ResourceNotFoundException

BusinessException

ConflictException

ValidationException
```

Assert

- Exception type
- Error message
- Error code (if applicable)

---

# Mapper Tests

Verify

- Request → Entity
- Entity → Response
- Nested Objects
- Collections
- Null Handling

Every mapper should have dedicated tests.

---

# Business Rule Tests

Every business rule requires

Positive scenario

Negative scenario

Boundary scenario

Examples

```
Booking available

Booking overlaps

Maximum booking reached
```

---

# Repository Query Tests

Verify

- Derived queries
- JPQL
- Native queries
- Specifications
- EntityGraph

Ensure generated SQL behaves correctly.

---

# Transaction Tests

Verify

- Commit
- Rollback
- Read-only transactions
- Concurrent updates

Never assume transactions work.

---

# Performance Tests

Only generate when explicitly requested.

Possible tools

```
JMH

Gatling

k6
```

Measure

- Throughput
- Latency
- Allocation
- Memory

---

# Test Data

Prefer reusable fixtures.

Examples

```
BookingTestData

CourseFixture

UserBuilder
```

Avoid repeated inline setup.

---

# Assertions

Prefer

```
AssertJ
```

Good

```java
assertThat(result)
        .isNotNull()
        .extracting(BookingResponse::status)
        .isEqualTo(CONFIRMED);
```

Avoid weak assertions.

---

# Mock Verification

Verify only important interactions.

Good

```java
verify(repository).save(entity);
```

Avoid excessive

```
verifyNoMoreInteractions()
```

unless justified.

---

# Time Handling

Avoid

```
LocalDateTime.now()
```

inside tests.

Inject

```
Clock
```

or fixed timestamps.

Tests must be deterministic.

---

# Random Data

Avoid uncontrolled randomness.

Prefer

- Builders
- Fixed fixtures
- Seeded generators

---

# Async Testing

Verify

- Event completion
- Retry behavior
- Timeout handling

Avoid

```
Thread.sleep()
```

Prefer Awaitility if project supports it.

---

# Database Testing

Prefer

Testcontainers

over

In-memory databases

when database behavior matters.

---

# API Testing

Verify

- Status Code
- Headers
- Response Body
- Validation Errors
- Authentication
- Pagination
- Sorting
- Filtering

---

# Coverage Guidelines

Target

```
Service
95%+

Repository
90%+

Controller
90%+
```

Coverage should not replace meaningful assertions.

---

# Code Quality

Tests should be

- Small
- Independent
- Repeatable
- Readable
- Fast

One logical behavior per test.

---

# Anti-Patterns

Never generate

- Sleeping tests
- Time-dependent assertions
- Order-dependent tests
- Shared mutable state
- Hardcoded database IDs
- Massive setup methods
- Testing private methods
- Multiple unrelated assertions
- Mocking class under test

---

# AI Review Checklist

Before returning tests verify

✓ Happy path covered

✓ Failure path covered

✓ Validation tested

✓ Business rules tested

✓ Exceptions tested

✓ Security tested

✓ Transactions considered

✓ Proper assertions

✓ Deterministic execution

✓ Readable tests

✓ Production-ready

---

# Definition of Done

Testing is complete only when

- Business rules verified
- Validation covered
- Exceptions covered
- Transactions verified
- Security tested
- Repository queries tested
- Integration tests added where required
- Tests are deterministic
- No flaky behavior
- Easy to maintain

---

# Example Invocations

### Generate Service Tests

```
Generate BookingService tests.
```

The AI should generate

- Unit tests
- Mock dependencies
- Verify business rules
- Verify repository interaction
- Verify exceptions
- Verify events

---

### Generate Controller Tests

```
Generate CourseController tests.
```

The AI should generate

- MockMvc tests
- Validation tests
- Authentication tests
- Response verification
- Error handling tests

---

### Review Tests

```
Review BookingServiceTest.java
```

The AI should verify

- Coverage
- Readability
- Missing scenarios
- Flaky behavior
- Mock correctness
- Assertion quality
- Maintainability
- Production readiness

---

### Improve Existing Tests

```
Improve EnrollmentRepository tests.
```

The AI should

- Increase coverage
- Remove duplication
- Improve assertions
- Add edge cases
- Improve readability
- Preserve existing behavior
- Follow project testing standards