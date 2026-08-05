# Bug Fix Workflow

> **Purpose**
>
> This workflow provides a structured process for analyzing, debugging, fixing, validating, and deploying production bugs in a Java Spring Boot microservice.

The objective is not just to make the error disappear, but to identify the **true root cause**, implement the **minimal safe fix**, and prevent future regressions.

---

# Guiding Principles

Always

- Reproduce before fixing
- Understand before coding
- Fix the root cause
- Minimize code changes
- Preserve backward compatibility
- Add regression tests
- Verify non-functional impact

Never

- Blindly change code
- Comment out logic
- Ignore failing tests
- Assume logs tell the whole story
- Merge without verification

---

# Overall Workflow

```
Bug Report
      │
      ▼
Understand Problem
      │
      ▼
Reproduce Issue
      │
      ▼
Collect Evidence
      │
      ▼
Identify Root Cause
      │
      ▼
Design Fix
      │
      ▼
Implement Fix
      │
      ▼
Regression Testing
      │
      ▼
Code Review
      │
      ▼
Deploy (if asked)
      │
      ▼
Production Verification (if asked)
      │
      ▼
Postmortem (if required)
```

---

# Step 1 — Understand the Bug

Collect

- Bug description
- Expected behavior
- Actual behavior
- Steps to reproduce
- Environment
- Frequency
- Severity
- Impact
- First occurrence
- Recent deployments

Example

```
Booking confirmation fails only
when payment succeeds after retry.
```

---

# Step 2 — Gather Context

Collect

- Request payload
- Response
- Logs
- Stack trace
- Correlation ID
- User ID
- Environment
- Timestamp                           
- Service version

Useful sources

Application logs through MCP server of below-mentioned observability tool, or you can ask developer to provide logs
- Kibana
- Splunk
- Datadog                                            
- Grafana
- CloudWatch
- APM traces

---

# Step 3 — Reproduce

Attempt reproduction

Local

↓

Integration

↓

QA

↓

Production-like

Document

- Exact inputs
- Database state
- API sequence
- Timing
- Configuration

Never skip reproduction unless impossible.

---

# Step 4 — Classify Bug

Possible categories

```
Business Logic

Validation

Database

Concurrency

Performance

Security

Caching

Transaction

Messaging

Configuration

Deployment

Infrastructure

External Dependency
```

---

# Step 5 — Root Cause Analysis

Ask

```
Why did this happen?
```

Apply

```
5 Whys
```

or

```
Fishbone Analysis
```

Avoid treating symptoms.

---

# Step 6 — Identify Impact

Determine

- Affected APIs
- Affected users
- Affected services
- Affected database tables
- Backward compatibility
- Data corruption
- Financial impact

---

# Step 7 — Verify Existing Tests

Check

- Unit Tests
- Integration Tests
- Controller Tests
- Repository Tests
- E2E Tests

Question

```
Why didn't tests catch this?
```

---

# Step 8 — Design the Fix

Prefer

Small

Focused

Safe

Readable

Maintainable

Document

- Cause
- Solution
- Risks
- Alternatives considered

---

# Step 9 — Implement

Guidelines

- Preserve existing behavior
- Avoid unrelated refactoring
- Follow coding standards
- Keep methods small
- Respect layering
- Preserve transactions

---

# Step 10 — Add Regression Test

Every bug must receive

At least one failing test before the fix.

Example

```
Bug

↓

Write failing test

↓

Apply fix

↓

Test passes
```

Never merge without regression coverage.

---

# Step 11 — Validate

Run

```
Unit Tests

Integration Tests

Repository Tests

Controller Tests
```

Verify

- Existing behavior
- New behavior
- Edge cases

---

# Step 12 — Review Performance

Check

- SQL queries
- N+1 issues
- Additional API calls
- Object allocation
- Lock contention
- Memory impact

Never introduce performance regressions.

---

# Step 13 — Security Review

Ensure

- Authorization preserved
- Validation preserved
- No sensitive logging
- No new vulnerabilities
- OWASP Top 10 unaffected

---

# Step 14 — Code Review Checklist

Reviewer verifies

✓ Root cause fixed

✓ Minimal change

✓ No dead code

✓ Tests added

✓ Readability maintained

✓ Naming acceptable

✓ Transactions correct

✓ Logging appropriate

✓ Performance acceptable

✓ Security maintained

---

# Step 15 — Deployment

Deploy

Dev

↓

QA

↓

Staging

↓

Production

Prefer

- Canary
- Blue-Green
- Rolling Deployment

Monitor immediately after release.

---

# Step 16 — Production Verification

Monitor

- Error rate
- API latency
- CPU
- Memory
- Database
- Queue depth
- Logs
- Business KPIs

Confirm

Bug no longer occurs.

---

# Step 17 — Postmortem

For critical bugs document

## Summary

```
Issue

Impact

Timeline

Resolution
```

## Root Cause

Technical explanation.

## Prevention

Examples

- Better validation
- More tests
- Better monitoring
- Improved documentation

---

# Bug Classification Matrix

| Severity | Description | SLA |
|----------|-------------|-----|
| Critical | System unavailable, data corruption | Immediate |
| High | Major feature unusable | Same day |
| Medium | Feature partially affected | 1–3 days |
| Low | Cosmetic/minor issue | Planned release |

---

# Logging Checklist

Verify logs include

- Correlation ID
- Request ID
- User ID
- Resource ID
- Error code

Never log

- Passwords
- Tokens
- Secrets
- Credit card numbers
- Sensitive PII

---

# Database Checklist

Verify

- Transactions
- Isolation
- Locking
- Deadlocks
- Constraints
- Index usage
- Execution plan
- Data consistency

---

# Concurrency Checklist

Look for

- Race conditions
- Deadlocks
- Lost updates
- Optimistic lock failures
- Pessimistic lock issues
- Thread safety

---

# Distributed System Checklist

Check

- Retry logic
- Idempotency
- Event ordering
- Duplicate messages
- Circuit breakers
- Timeouts
- Partial failures
- Saga compensation

---

# API Checklist

Verify

- Request validation
- Response contract
- Backward compatibility
- Error handling
- HTTP status codes
- Pagination
- Authentication
- Authorization

---

# AI Investigation Prompt

When debugging, the AI should answer

1. What is the observed issue?

2. Can it be reproduced?

3. What is the exact root cause?

4. Which code is responsible?

5. Which component owns the bug?

6. Smallest possible fix?

7. Side effects?

8. Required tests?

9. Performance impact?

10. Security impact?

11. Deployment risk?

12. Long-term prevention?

---

# Deliverables

A completed bug fix should include

```
Root Cause Analysis

Files Changed

Reason for Change

Regression Test

Impact Analysis

Risk Assessment

Verification Steps

Deployment Notes
```

---

# AI Output Format

## Bug Summary

```
...
```

## Root Cause

```
...
```

## Proposed Fix

```
...
```

## Files to Modify

```
Controller

Service

Repository

Mapper

Entity

Configuration
```

## Tests

```
Unit Test

Integration Test

Regression Test
```

## Risk Assessment

```
Low

Medium

High
```

## Validation Checklist

```
✓ Builds Successfully

✓ Tests Passing

✓ Regression Covered

✓ Security Reviewed

✓ Performance Reviewed

✓ Ready for Production
```

---

# Definition of Done

A bug is considered fixed only when

- Root cause identified
- Minimal fix implemented
- Regression test added
- Existing tests pass
- No performance regression
- No security regression
- Code reviewed
- Successfully deployed
- Production monitoring confirms resolution
- Documentation updated if applicable