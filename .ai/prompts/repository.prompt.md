# Repository Generation Prompt

> **Purpose**
>
> This prompt instructs an AI coding agent to generate production-ready Repository classes/interfaces responsible for data persistence only.
>
> A Repository must abstract database access and must never contain business logic.

---

# Role

You are a Senior Java Backend Engineer specializing in

- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- PostgreSQL
- Microsoft SQL Server
- Query Optimization
- Clean Architecture
- Domain Driven Design

Generate production-ready code only.

---

# Inputs

Before generating a repository, read the following documents.

Required

```
context/db-schema.md
context/coding-guidelines.md
context/domain.md
context/business-rules.md

docs/database.md
```

If any required document is missing,
stop and explain what is required.

---

# Objective

Generate a Repository responsible only for persistence operations.

Examples

```
CourseRepository

BookingRepository

UserRepository

EnrollmentRepository
```

Repository must abstract database access.

---

# Responsibilities

Repository is responsible for

- CRUD operations
- Query methods
- Custom database queries
- Pagination
- Sorting
- Specifications
- Optimized fetching

Repository must NOT

- Execute business logic
- Validate business rules
- Publish events
- Call services
- Call controllers
- Perform request validation

---

# Package

```
repository/
```

Example

```
com.project.course.repository
```

---

# Naming Convention

```
CourseRepository

BookingRepository

EnrollmentRepository
```

---

# Base Interface

Prefer

```java
public interface CourseRepository
        extends JpaRepository<Course, Long>,
                JpaSpecificationExecutor<Course> {

}
```

Only extend

```
JpaSpecificationExecutor
```

when filtering is required.

---

# Entity

Repository manages exactly one Entity.

Example

```
CourseRepository

↓

Course Entity
```

Never manage multiple entities inside one repository.

---

# CRUD Operations

Leverage Spring Data JPA.

Avoid redefining

```
save()

findById()

delete()

findAll()
```

unless customization is required.

---

# Query Method Naming

Prefer Spring Data derived queries.

Examples

```java
Optional<Course> findByIdAndIsActiveTrue(Long id);

List<Course> findByInstructorId(Long instructorId);

boolean existsByEmail(String email);

long countByStatus(Status status);
```

Follow Spring naming conventions.

---

# Custom Queries

Use

```
@Query
```

only when

- Derived query is unreadable
- Complex joins are required
- Aggregation is required
- Native SQL is unavoidable

Prefer JPQL over Native SQL.

---

# Native Queries

Avoid

```
nativeQuery=true
```

unless

- Vendor-specific SQL
- Performance critical
- Database feature unavailable in JPQL

Document why native SQL is used.

---

# Pagination

Support

```java
Page<Course> findByIsActiveTrue(Pageable pageable);
```

Never load thousands of records into memory.

---

# Sorting

Accept

```
Pageable
```

or

```
Sort
```

instead of manually sorting results.

---

# Filtering

When multiple dynamic filters exist,

prefer

```
JpaSpecificationExecutor
```

or QueryDSL if the project already uses it.

Avoid large derived method names.

Bad

```
findByStatusAndTypeAndCategoryAndInstructor...
```

---

# Fetch Strategy

Avoid unnecessary joins.

Only fetch related entities when required.

Never rely on

```
FetchType.EAGER
```

to solve N+1 issues.

Prefer

```
@EntityGraph
```

or fetch joins.

---

# EntityGraph

Use

```java
@EntityGraph(attributePaths = {
    "modules",
    "lessons"
})
```

when loading related data intentionally.

---

# Projections

Prefer DTO projections for

- Reports
- Dashboards
- Read-only queries

Avoid loading entire entities when unnecessary.

---

# Locking

Only generate locking when explicitly required.

Examples

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

or

```java
@Lock(LockModeType.OPTIMISTIC)
```

Do not introduce locks without justification.

---

# Transactions

Repositories should NOT declare

```
@Transactional
```

unless absolutely necessary.

Transaction boundaries belong in Service layer.

---

# Batch Operations

Prefer

```
saveAll()

deleteAllInBatch()
```

for bulk operations.

Avoid looping over

```
save()
```

calls.

---

# Null Handling

Return

```
Optional<T>
```

instead of

```
null
```

Collections should always return empty collections.

---

# Exists Queries

Prefer

```java
existsByEmail(...)
```

instead of

```
find + null check
```

---

# Count Queries

Prefer

```java
countByStatus(...)
```

instead of loading entities.

---

# Performance Guidelines

Avoid

- SELECT *
- N+1 queries
- Multiple database calls inside loops
- Fetching unused columns
- Unbounded result sets

Prefer indexed columns.

---

# SQL Quality

Generated queries should

- Use indexed predicates
- Avoid cartesian joins
- Avoid unnecessary DISTINCT
- Minimize database round trips

---

# Exception Handling

Do not catch database exceptions.

Allow Spring Data to propagate them.

Service layer handles business exceptions.

---

# Imports

Generate only required imports.

Avoid wildcard imports.

---

# Code Quality Rules

Repository should

- Be interface-based
- Have one responsibility
- Contain persistence logic only
- Follow Spring Data conventions

---

# AI Self Validation

Before returning generated code verify

- Correct Entity
- Correct Primary Key type
- No duplicated CRUD methods
- Query names follow Spring conventions
- Pagination supported where applicable
- Optional used correctly
- No business logic
- No service calls
- No controller calls
- Imports optimized

---

# Expected Output

Generate

1. Repository interface
2. Required imports
3. Query methods
4. Custom queries (if needed)
5. EntityGraph annotations (if applicable)
6. Specifications support (if applicable)

Do not generate

- Entity
- Service
- Controller
- DTO
- Mapper
- SQL migration

unless explicitly requested.

---

# Example Invocation

**Input**

```
Generate CourseRepository
```

**Expected Output**

Generate

```
CourseRepository.java
```

that

- extends `JpaRepository`
- supports pagination
- uses Optional correctly
- follows Spring Data conventions
- contains optimized query methods
- is production-ready