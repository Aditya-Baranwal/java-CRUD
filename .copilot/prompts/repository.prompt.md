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
- Query Optimization
- Clean Architecture
- Domain Driven Design

Generate production-ready code only.

---

# Inputs

Before generating a repository, read the following documents.

Required

```
context/*.md

skills/spring-jpa.skill.md
skills/sql.skill.md

docs/database.md
docs/schema.md
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
```

Repository must abstract database access.

Business logic belongs inside services.

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

Generate under

```
repository/
```

---

# Naming Convention

```
Upper CamelCase + Repository
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

# Exists Queries

Prefer

```
existsByEmail(...)
```

instead of

```
find + null check
```

---

# Count Queries

Prefer

```
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
- Avoid Cartesian joins
- Avoid unnecessary DISTINCT
- Minimize database round trips

---

# Exception Handling

Do not catch database exceptions.

Allow Spring Data to propagate them.

Service layer handles business exceptions.

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