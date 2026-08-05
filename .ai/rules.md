# AI Coding Rules

> **Purpose**
>
> This document defines the mandatory rules the AI Coding Assistant must follow while analyzing, generating, modifying, reviewing, or debugging code.
>
> Unlike `instruction.md`, which describes overall behavior, this document contains **non-negotiable rules**.

Violation of these rules should be treated as an error.

---

# Rule Priority

When multiple rules exist, follow this precedence.

```
1. User Request

↓

2. System Instructions

↓

3. Project Instructions

↓

4. Workflow

↓

5. Coding Guidelines

↓

6. Framework Best Practices
```

---

# General Rules

## R001 — Never Invent Information

Never invent

- APIs
- Database tables
- Fields
- Endpoints
- Configurations
- Business rules

If information is missing

- Ask for clarification
- State assumptions explicitly

---

## R002 — Minimize Changes

Modify only the code necessary.

Avoid

- Large refactoring
- Formatting-only commits
- Renaming unrelated variables
- Unnecessary optimizations

---

## R003 — Preserve Existing Behaviour

Unless explicitly requested

Do NOT

- Change API contracts
- Change business rules
- Change database behaviour
- Change validation
- Change security

---

## R004 — Reuse Existing Code

Before generating

Search for

- Existing service
- Existing repository
- Existing DTO
- Existing mapper
- Existing utility

Avoid duplicate implementations.

---

# Architecture Rules

## R100 — Respect Layering

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

Never

```
Controller → Repository

Controller → Database

Repository → Service
```

---

## R101 — One Responsibility Per Layer

Controller

- HTTP

Service

- Business Logic

Repository

- Persistence

Mapper

- Object Mapping

DTO

- API Contract

Entity

- Persistence Model

---

# Controller Rules

## R200

Controllers must

- Validate requests
- Return DTOs
- Call services

Controllers must never

- Execute SQL
- Contain business logic
- Open transactions
- Access repositories

---

# Service Rules

## R300

Services own

- Business rules
- Transactions
- Validation
- External integrations

Services must never

- Return entities to APIs
- Access HTTP request objects
- Execute SQL directly

---

# Repository Rules

## R400

Repositories

- Perform persistence only

Repositories must never

- Validate business rules
- Call services
- Publish events

Prefer

- Query methods
- Specifications
- JPQL

---

# Entity Rules

## R500

Entities should

- Use LAZY loading
- Include audit fields
- Include optimistic locking where appropriate

Avoid

- Business logic
- EAGER loading by default
- CascadeType.ALL unless justified

---

# DTO Rules

## R600

Always

Separate

```
Request DTO

Response DTO
```

Never expose entities outside the persistence layer.

---

# Validation Rules

## R700

Structural validation

↓

DTO

Business validation

↓

Service

Never duplicate validation across layers.

---

# Transaction Rules

## R800

Write

```
@Transactional
```

Read

```
@Transactional(readOnly = true)
```

Never place transactions in controllers.

---

# Exception Rules

## R900

Throw

```
ResourceNotFoundException

ConflictException

BusinessException

ValidationException
```

Never

```
catch(Exception)
```

unless absolutely required.

Use

```
@RestControllerAdvice
```

for centralized handling.

---

# Logging Rules

## R1000

Always log

- Business events
- Resource IDs
- Correlation IDs

Never log

- Passwords
- JWT Tokens
- Secrets
- API Keys
- Sensitive PII

Use

```
INFO

WARN

ERROR
```

appropriately.

---

# Security Rules

## R1100

Always verify

- Authentication
- Authorization
- Input Validation
- Ownership
- Sensitive data exposure

Review against

OWASP Top 10.

---

# Database Rules

## R1200

Always review

- Indexes
- Constraints
- Foreign Keys
- Locking
- Query efficiency

Never

```
SELECT *

UPDATE without WHERE

DELETE without WHERE
```

---

# Performance Rules

## R1300

Review

- N+1 Queries
- Missing pagination
- Duplicate database calls
- Blocking operations
- Memory allocation
- Algorithm complexity

Optimize only when justified.

---

# Testing Rules

## R1400

Every feature

↓

Unit Test

↓

Integration Test

Every bug fix

↓

Regression Test

Never leave new behaviour untested.

---

# Review Rules

## R1500

Review

- Correctness
- Security
- Performance
- Maintainability
- Readability
- Testing
- Documentation

Classify issues

```
Critical

High

Medium

Low
```

---

# Runtime Investigation Rules

## R1600

When debugging production issues

Collect

- Stack Trace
- Request
- Response
- Timestamp
- Correlation ID
- Environment

If observability tools are available

Use

- Datadog
- Splunk
- Kibana
- CloudWatch
- Grafana
- APM

Otherwise

Request runtime evidence from the user.

Never fabricate logs or metrics.

---

# Tool Rules

## R1700

If tools exist

Use them.

Examples

```
GitHub

GitLab

Terminal

Docker

Kubernetes

SonarQube

Datadog
```

Do not guess information that tools can provide.

---

# AI Output Rules

## R1800

Always include

```
Summary

Assumptions

Implementation

Testing

Risks

Recommendations
```

when appropriate.

---

# Documentation Rules

## R1900

Whenever implementation changes

Update

- API Documentation
- Migration Notes
- README
- Design Docs

if impacted.

---

# Bug Fix Rules

## R2000

Never fix symptoms only.

Always

```
Reproduce

↓

Collect Evidence

↓

Find Root Cause

↓

Implement Minimal Fix

↓

Regression Test

↓

Validate
```

---

# Feature Rules

## R2100

Generate code in order

```
Migration

↓

Entity

↓

Repository

↓

DTO

↓

Mapper

↓

Service

↓

Controller

↓

Tests
```

---

# Code Quality Rules

## R2200

Prefer

- Constructor Injection
- SOLID
- Composition
- Small Methods
- Clear Naming
- Immutable Objects where practical

Avoid

- God Classes
- Static State
- Field Injection
- Duplicate Code
- Magic Numbers

---

# AI Behaviour Rules

## R2300

If information is missing

- Ask questions
- State assumptions

Do not fabricate missing details.

If context conflicts

Follow the defined priority order.

---

# Completion Rules

## R2400

A task is complete only if

✓ Requirements satisfied

✓ Architecture respected

✓ Code compiles

✓ Validation complete

✓ Transactions correct

✓ Security reviewed

✓ Performance reviewed

✓ Tests included

✓ Documentation updated (if affected)

✓ No critical issues remain

---

# Absolute Prohibitions

The AI must never

- Invent production data
- Invent logs
- Invent stack traces
- Invent APIs
- Invent database schemas
- Bypass service layer
- Expose secrets
- Return entities directly from APIs
- Ignore compilation errors
- Ignore failing tests
- Recommend unsafe production changes without warning

---

# Golden Rules

1. **Correctness over speed.**
2. **Smallest safe change wins.**
3. **Evidence before assumptions.**
4. **Architecture before implementation.**
5. **Security before convenience.**
6. **Tests are part of the implementation.**
7. **If you don't know, ask—don't guess.**