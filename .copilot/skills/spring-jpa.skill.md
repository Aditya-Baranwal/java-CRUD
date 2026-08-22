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

```
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

Use

```
@Id
@GeneratedValue(...)
```

Strategy should follow project convention.

Preferred strategies, do not invent new ones

```
IDENTITY
SEQUENCE
```

Distributed systems may use

```
UUID
```

Primary keys must never change.

---

# Column Mapping

Explicitly map every column.

Example

```java
@Column(name = "course_title", nullable = false, length = 200)
private String courseTitle;
```

Avoid relying on default mappings.

---

# Relationships

Generate relationships according to database design.

Supported

```
@OneToOne

@OneToMany

@ManyToOne

@ManyToMany
```

Always determine based on the schema, if it is unclear, ask for clarification.

- Owning side
- Inverse side
- Cascade behavior
- Fetch strategy

Never 

- create unnecessary bidirectional relationships.

---

# Fetch Strategy

Default

```
FetchType.LAZY
```

Avoid unnecessary joins.

Only fetch related entities when required.

Never use

```
FetchType.EAGER
```

to solve N+1 issues, unless explicitly justified.

Solve loading problems using

- Fetch Join
- EntityGraph
- DTO Projection

---

# EntityGraph

Use

```
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

condition to add

- when there is a parent-child relationship 
- when child cannot exist without the parent.
- when it is mentioned in design

If it is unclear, then ask to confirm

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

If project supports auditing, include

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

or spring data auditing, project specific auditing.

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

# Enumerations

Always map enums explicitly.

Preferred

```
@Enumerated(EnumType.STRING)
```

Never use ordinal mapping.

---

# JSON Columns

If schema specifies JSON,

generate appropriate mapping.

Example

```java
@Column(columnDefinition = "jsonb")
private String metadata;
```

or use project-specific converters.

---

# LOB Columns

Large text

```
@Lob
```

Large binary files should not be stored in entities unless explicitly required.

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

```
findByEmail()
existsByEmail()
countByStatus()
findByStatusOrderByCreatedAtDesc()
```

Avoid unnecessarily long derived method names.

---

# Filtering

When multiple dynamic filters exist,

prefer

```
JpaSpecificationExecutor
```

for dynamic filtering.

or QueryDSL if the project already uses it.

Avoid large derived method names.

Bad

```
findByStatusAndTypeAndCategoryAndInstructor...
```

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
Page<Course> findByIsActiveTrue(Pageable pageable);
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
Pageable
```

or

```
Sort
```

instead of manually sorting results.

---

# Transactions

Service layer owns transactions.

Read

```
@Transactional(readOnly = true)
```

Write

```
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

```
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

Examples

```
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

or

```
@Lock(LockModeType.OPTIMISTIC)
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

- Flyway or Liquibase migration, ask which one to use if it is not mentioned
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

- Correct entity mapping
- Explicit column mappings
- LAZY relationships
- Proper repository methods
- Pagination supported
- No N+1 issues
- Optimistic locking considered
- Dirty checking used correctly
- Transactions in Service layer
- Imports optimized
- Production-ready

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