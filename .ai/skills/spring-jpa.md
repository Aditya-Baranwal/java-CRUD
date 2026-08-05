# Spring Data JPA Development Skill

> **Skill Name**
>
> Production-Grade Spring Data JPA & Hibernate Development

---

# Purpose

This skill defines how an AI agent should design, implement, review, and optimize the persistence layer using Spring Data JPA and Hibernate.

The AI should generate code that is

- Production-ready
- Performant
- Maintainable
- Scalable
- Transactionally correct
- Database efficient

The persistence layer should abstract database access while preserving business correctness.

---

# Technology Stack

Default assumptions

```
Java 21
Spring Boot 3.x
Spring Data JPA
Hibernate 6.x
PostgreSQL
Flyway
```

If the project already uses another JPA provider or database,
follow existing conventions.

---

# JPA Philosophy

Always follow

- Persistence Ignorance
- Repository Pattern
- Unit of Work
- Identity Map
- Transactional Consistency
- Optimistic Concurrency

Entities model persistent state—not business workflows.

---

# Layer Responsibilities

```
Controller
        ↓
Service
        ↓
Repository
        ↓
Database
```

Repository owns persistence.

Service owns transactions.

Controller never accesses repositories directly.

---

# Package Structure

```
entity/

repository/

mapper/

specification/

projection/

converter/
```

---

# Entity Design

Every entity should

- Represent one table
- Have one responsibility
- Match the schema
- Use explicit mappings

Required

```java
@Entity
@Table
```

Use explicit

```
@Column
```

annotations for all fields.

---

# Primary Keys

Preferred

```
BIGINT
IDENTITY
```

or project standard.

Distributed systems may use

```
UUID
```

Primary keys must never change.

---

# Relationships

Supported

```
@OneToOne

@OneToMany

@ManyToOne

@ManyToMany
```

Always identify

- Owning side
- Inverse side
- Cascade
- Fetch strategy

Never create unnecessary bidirectional relationships.

---

# Fetch Strategy

Default

```java
FetchType.LAZY
```

Never use

```java
FetchType.EAGER
```

unless explicitly justified.

Solve loading problems using

- EntityGraph
- Fetch Join
- DTO Projection

---

# Cascade Rules

Use only required cascade types.

Preferred

```
PERSIST

MERGE
```

Avoid

```
CascadeType.ALL
```

unless the aggregate truly owns the lifecycle.

---

# Orphan Removal

Use

```
orphanRemoval = true
```

only for true parent-child ownership.

---

# Equals & HashCode

Base equality on

- Immutable business key
- Primary key (project convention)

Never include relationships.

Avoid recursive equality.

---

# ToString

Exclude

```
@OneToMany

@ManyToMany
```

relationships.

Avoid recursive object graphs.

---

# Audit Fields

Include when supported

```
createdAt

updatedAt

createdBy

updatedBy
```

Use

```
@CreationTimestamp

@UpdateTimestamp
```

or Spring Data Auditing.

---

# Optimistic Locking

Use

```java
@Version
private Long version;
```

for concurrent updates.

Prefer optimistic locking over pessimistic locking.

---

# Repository Design

Repositories should

- Extend JpaRepository
- Support pagination
- Support sorting
- Support specifications

Repository contains persistence logic only.

---

# Query Methods

Prefer derived query methods.

Examples

```java
findByEmail()

existsByEmail()

countByStatus()

findByStatusOrderByCreatedAtDesc()
```

Avoid unnecessarily long derived method names.

---

# Specifications

Use

```
JpaSpecificationExecutor
```

for dynamic filtering.

Avoid multiple overloaded query methods.

---

# Custom Queries

Prefer

```
JPQL
```

Use

```
@Query
```

only when

- Complex joins
- Aggregations
- Performance optimization

Prefer JPQL over native SQL.

---

# Native Queries

Avoid unless

- Database-specific feature
- Proven performance benefit
- JPQL limitation

Document why native SQL is required.

---

# Pagination

Always use

```java
Pageable
```

Never fetch unlimited records.

For very large datasets,

prefer

```
Keyset Pagination
```

when applicable.

---

# Sorting

Use

```
Sort

Pageable
```

Never sort manually after fetching.

---

# EntityGraph

Use

```java
@EntityGraph(attributePaths = {
    "modules",
    "lessons"
})
```

to eliminate N+1 queries when required.

Do not over-fetch data.

---

# DTO Projection

Prefer projections for

- Reports
- Dashboards
- Search APIs
- Read-only endpoints

Avoid loading complete entities unnecessarily.

---

# Transactions

Service layer owns transactions.

Read

```java
@Transactional(readOnly = true)
```

Write

```java
@Transactional
```

Repositories should not manage transactions.

---

# Persistence Context

Understand

- Managed Entity
- Detached Entity
- Removed Entity
- Transient Entity

Avoid unnecessary

```
merge()
```

operations.

Let JPA dirty checking persist updates.

---

# Dirty Checking

Prefer

```java
entity.update(...);
```

inside transactions.

Avoid unnecessary

```
save()
```

on already managed entities.

---

# Locking

Use only when necessary.

Supported

```
Optimistic

Pessimistic Read

Pessimistic Write
```

Always justify locking strategy.

---

# Batch Operations

Prefer

```
saveAll()

deleteAllInBatch()
```

Configure batch size when processing large datasets.

Avoid repeated

```
save()
```

inside loops.

---

# Performance

Always check for

- N+1 queries
- Cartesian joins
- Duplicate queries
- LazyInitializationException
- Unbounded result sets

Optimize database round trips.

---

# Index Usage

Frequently filtered columns should be indexed.

Examples

```
email

status

created_at

foreign_keys
```

Avoid querying non-indexed columns in large tables.

---

# Null Handling

Return

```
Optional<T>
```

for single entity lookups.

Collections should never return

```
null
```

---

# Soft Delete

Prefer

```
deleted_at

is_active
```

over physical deletion when required by business.

Keep repository methods aware of soft-delete strategy.

---

# Validation

Persistence layer validates

- Constraints
- Relationships

Business validation belongs in Service.

---

# Exception Handling

Allow Spring Data exceptions to propagate.

Translate to business exceptions only in Service layer.

Do not catch generic database exceptions in repositories.

---

# Testing

Generate

### Repository Tests

```
@DataJpaTest
```

Verify

- Query correctness
- Relationships
- Pagination
- Constraints
- Transactions

Use Testcontainers when supported.

---

# Migration Strategy

Every schema change must include

- Flyway migration
- Backward compatibility
- Roll-forward strategy

Never modify executed migrations.

---

# Common Hibernate Pitfalls

Avoid

- LazyInitializationException
- N+1 Queries
- Multiple Bag Fetch
- Excessive Cascade.ALL
- EAGER fetching
- Open Session In View reliance
- Detached entity misuse
- Manual flush() without need

---

# AI Review Checklist

Before returning code verify

✓ Correct entity mapping

✓ Explicit column mappings

✓ LAZY relationships

✓ Proper repository methods

✓ Pagination supported

✓ No N+1 issues

✓ Optimistic locking considered

✓ Dirty checking used correctly

✓ Transactions in Service layer

✓ Imports optimized

✓ Production-ready

---

# Anti-Patterns

Never generate

- Business logic in Entity
- Repository calling Service
- Controller accessing Repository
- EAGER relationships by default
- CascadeType.ALL everywhere
- Native SQL without justification
- Manual entity mapping when mapper exists
- SELECT *
- save() inside loops
- Transactions in Controller
- Returning entities directly from APIs

---

# Definition of Done

Persistence implementation is complete only when

- Entity mapping correct
- Relationships validated
- Repository implemented
- Pagination supported
- Transactions correctly placed
- Queries optimized
- N+1 avoided
- Index usage considered
- Tests written
- Flyway migration prepared
- Ready for production

---

# Example Invocations

### Generate Persistence Layer

```
Implement Course persistence layer
```

The AI should generate

- Entity
- Repository
- Specifications
- Projections (if needed)
- Flyway migration
- Repository tests

following Spring Data JPA best practices.

---

### Review Repository

```
Review BookingRepository.java
```

The AI should verify

- Query correctness
- Performance
- Index usage
- Fetch strategy
- Pagination
- Transactions
- Hibernate best practices

---

### Optimize JPA

```
Optimize EnrollmentService persistence
```

The AI should

- Remove N+1 queries
- Introduce EntityGraph if needed
- Improve repository methods
- Reduce database round trips
- Preserve business behavior
- Follow Spring Data JPA best practices