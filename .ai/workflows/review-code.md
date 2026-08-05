# Code Review Workflow

> **Purpose**
>
> This workflow provides a systematic process for reviewing code changes in a production-grade Java Spring Boot microservice.
>
> The goal is to ensure that every change is:
>
> - Functionally Correct
> - Maintainable
> - Secure
> - Performant
> - Testable
> - Production Ready

Unlike traditional reviews, this workflow is designed for both **human reviewers** and **AI agents**.

---

# AI Capabilities

Before starting, determine available capabilities.

```
Source Code
Configuration Files
Git Diff
Pull Request
Build Logs
CI Results
Unit Tests
Design Documents
```

Optional Tools

```
GitHub
GitLab
Bitbucket
SonarQube
Jira
Datadog
Kibana
Splunk
CloudWatch
Grafana
Terminal
Docker
Kubernetes
```

If a required source is unavailable, request it before continuing.

---

# Review Workflow

```
Pull Request
      │
      ▼
Understand Requirement
      │
      ▼
Review Design
      │
      ▼
Review Architecture
      │
      ▼
Review Implementation
      │
      ▼
Review Security
      │
      ▼
Review Performance
      │
      ▼
Review Testing
      │
      ▼
Review Documentation
      │
      ▼
Generate Findings
      │
      ▼
Approve / Request Changes
```

---

# Step 1 — Understand the Change

Collect

- Business requirement
- User story
- Bug report
- Design document
- Acceptance criteria
- Pull request description

Questions

- What problem is being solved?
- Is the implementation aligned with the requirement?
- Are there simpler alternatives?

---

# Step 2 — Review Scope

Verify

- Modified files
- New classes
- Deleted files
- Configuration changes
- Database migrations
- API changes

Ensure no unrelated changes are included.

---

# Step 3 — Architecture Review

Verify

```
Controller
      ↓
Service
      ↓
Repository
```

Review

- Layering
- SOLID
- Separation of Concerns
- Package structure
- Dependency Injection
- Circular dependencies

---

# Step 4 — Controller Review

Checklist

✓ Thin controller

✓ Bean Validation

✓ DTOs only

✓ REST conventions

✓ Correct HTTP status

✓ Pagination

✓ Security annotations

✓ No business logic

✓ No repository access

---

# Step 5 — Service Review

Verify

- Business rules
- Transaction boundaries
- Validation
- Exception handling
- Event publishing
- External service orchestration
- Logging
- Idempotency

Review

```
@Transactional

readOnly usage

rollback behaviour
```

---

# Step 6 — Repository Review

Verify

- Query correctness
- Pagination
- Specifications
- EntityGraph
- Index-friendly queries
- No N+1
- Proper Optional usage
- No business logic

---

# Step 7 — Entity Review

Review

- Relationships
- LAZY fetching
- Cascade rules
- Constraints
- Indexes
- Optimistic Locking
- Audit fields
- equals/hashCode
- toString recursion

---

# Step 8 — DTO Review

Verify

- Request/Response separation
- Bean Validation
- No Entity exposure
- Correct field naming
- No unnecessary fields

---

# Step 9 — Mapper Review

Review

- Mapping completeness
- Null safety
- Collection mapping
- Nested mapping
- Update mapping

If MapStruct is used

Verify generated mappings are correct.

---

# Step 10 — Security Review

Review

Authentication

Authorization

Ownership

Validation

Secrets

Sensitive Logging

OWASP

Verify

✓ SQL Injection prevention

✓ XSS prevention

✓ Authorization preserved

✓ No sensitive logs

✓ Secure defaults

---

# Step 11 — Transaction Review

Review

- Transaction boundary
- Propagation
- Isolation
- Rollback
- Read-only optimization
- Nested transactions
- Distributed transactions

---

# Step 12 — Database Review

Review

- Flyway migration
- Constraints
- Foreign Keys
- Indexes
- Locking
- Execution plans
- Data consistency

---

# Step 13 — Performance Review

Look for

- N+1 Queries
- Repeated DB Calls
- Missing Pagination
- Large Collections
- Blocking Calls
- Memory Leaks
- Inefficient Streams
- Expensive Loops

Review

Time Complexity

Space Complexity

---

# Step 14 — Concurrency Review

Verify

- Thread Safety
- Synchronization
- Race Conditions
- Deadlocks
- Optimistic Locking
- Atomicity

---

# Step 15 — Exception Handling

Verify

- Domain Exceptions
- Global Exception Handler
- Error Codes
- HTTP Status
- Useful Messages

Avoid

```
catch(Exception)
```

unless absolutely necessary.

---

# Step 16 — Logging Review

Logs should include

- Correlation ID
- Request ID
- Resource ID

Never log

- Passwords
- Tokens
- Secrets
- PII

---

# Step 17 — Runtime Evidence Review (Optional)

Applicable only when reviewing a bug fix or production issue.

## Required Evidence

- Stack Trace
- Error Message
- Correlation ID
- Request Payload
- Response
- Timestamp
- Environment

Optional Sources

- Datadog
- Kibana
- Splunk
- CloudWatch
- Grafana
- APM Traces

Acquisition Strategy

```
IF observability tool available

    Review logs
    Review traces
    Review metrics

ELSE

    Ask for

    - logs
    - stack trace
    - correlation id
```

Review

- Does the change actually fix the root cause?
- Are similar failures still possible?
- Does the fix introduce regressions?

---

# Step 18 — Testing Review

Verify

Unit Tests

Integration Tests

Repository Tests

Controller Tests

Regression Tests

Review

✓ Happy Path

✓ Failure Path

✓ Edge Cases

✓ Validation

✓ Transactions

✓ Authorization

---

# Step 19 — API Review

Verify

- REST conventions
- Versioning
- DTO contracts
- Validation
- Error responses
- Pagination
- Backward compatibility

---

# Step 20 — Documentation Review

Review

- OpenAPI
- README
- ADR
- Design Docs
- Migration Notes
- Release Notes

---

# Step 21 — Production Readiness

Verify

✓ Configuration externalized

✓ Health checks

✓ Metrics

✓ Monitoring

✓ Feature Flags

✓ Backward compatibility

✓ Rollback strategy

---

# Common Anti-Patterns

Never approve

- Fat Controllers
- Repository in Controller
- Business logic in Repository
- Field Injection
- Static Spring Beans
- Catch(Exception)
- EAGER fetching by default
- Returning Entities from API
- Hardcoded Secrets
- Duplicate Logic
- Circular Dependencies

---

# Severity Classification

| Severity | Description | Action |
|----------|-------------|--------|
| Critical | Security, data corruption, system failure | Must Fix |
| High | Functional bug, transaction issue | Must Fix |
| Medium | Performance, maintainability | Should Fix |
| Low | Naming, formatting, readability | Optional |

---

# Review Output Format

## Summary

```
Purpose of Change

Overall Assessment
```

---

## Functional Review

```
✓ Requirement satisfied

✓ Business logic correct

✗ Missing edge case
```

---

## Architecture Findings

```
...
```

---

## Security Findings

```
...
```

---

## Performance Findings

```
...
```

---

## Testing Findings

```
...
```

---

## Documentation Findings

```
...
```

---

## Critical Issues

```
...
```

---

## Recommendations

```
...
```

---

## Overall Rating

```
★★★★★
```

---

## Decision

```
Approve

Approve with Comments

Request Changes
```

---

# AI Review Checklist

Before approving verify

✓ Requirement understood

✓ Scope reviewed

✓ Architecture respected

✓ Business logic correct

✓ Security reviewed

✓ Transactions reviewed

✓ Database reviewed

✓ Performance reviewed

✓ Concurrency reviewed

✓ Tests adequate

✓ Documentation updated

✓ Production ready

---

# Definition of Done

A code review is complete only when

- Requirements are satisfied
- Architecture follows project standards
- Business logic is correct
- No critical or high-severity issues remain
- Security review completed
- Performance review completed
- Database impact reviewed
- Tests are sufficient and passing
- Documentation updated
- Runtime evidence reviewed (when applicable)
- Code is approved for production deployment