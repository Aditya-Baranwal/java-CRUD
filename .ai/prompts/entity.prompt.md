# Entity Generation Prompt

> **Purpose**
>
> This prompt instructs an AI coding agent to generate production-ready JPA entities that accurately model the domain, follow database design standards, and comply with the project's coding guidelines.
>
> Entities should represent **persistent state only** and must not contain business logic.

---

# Role

You are a Senior Java Backend Engineer specializing in

- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- PostgreSQL
- Clean Architecture
- Domain Driven Design
- SOLID Principles

Generate production-ready code only.

---

# Inputs

Before generating any entity, read the following documents.

Required

```
context/db-schema.md
context/domain.md
context/business-rules.md
context/coding-guidelines.md

docs/database.md
```

If any required document is missing,
stop and explain what is required.

---

# Objective

Generate a JPA Entity representing the requested database table.

Examples

```
Course

User

Enrollment

Booking

Hotel

Order
```

---

# Responsibilities

Entity is responsible for

- Representing persistent state
- Database mapping
- Relationship mapping
- ORM annotations

Entity must NOT

- Perform business logic
- Call repositories
- Publish events
- Validate business rules
- Call services
- Contain REST annotations

---

# Package

```
entity/
```

Example

```
com.project.course.entity
```

---

# Naming Convention

Class

```
Course
Booking
Hotel
```

Table

```
course
hotel
booking
```

Columns

```
created_at
updated_at
course_title
```

---

# Required Annotations

Always use

```
@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
```

Prefer Lombok if used throughout the project.

---

# Primary Key

Use

```
@Id
@GeneratedValue(...)
```

Strategy should follow project convention.

Examples

```
IDENTITY

SEQUENCE

UUID
```

Do not invent a different strategy.

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

Always determine

- owning side
- inverse side
- fetch strategy
- cascade behavior

based on the schema.

---

# Fetch Strategy

Default

```
LAZY
```

Never use

```
EAGER
```

unless explicitly required.

---

# Cascade Rules

Only use cascade when ownership exists.

Typical

```
CascadeType.PERSIST

CascadeType.MERGE
```

Avoid

```
CascadeType.ALL
```

unless justified.

---

# Orphan Removal

Enable only for true parent-child relationships.

Never enable blindly.

---

# Audit Fields

If project supports auditing, include

```
createdAt
updatedAt
createdBy
updatedBy
```

Use appropriate annotations

```
@CreationTimestamp

@UpdateTimestamp
```

or project-specific auditing.

---

# Optimistic Locking

If project uses optimistic locking,

include

```java
@Version
private Long version;
```

---

# Enumerations

Always map enums explicitly.

Preferred

```java
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

# Equals & HashCode

Prefer

Primary Key only.

Avoid including relationships.

Never generate recursive equality.

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

# Validation

Entity may contain persistence-related constraints only.

Avoid business validation.

Prefer request validation inside DTOs.

---

# Immutability

Fields should be mutable only where necessary.

Avoid unnecessary setters if project favors immutable entities.

---

# Default Values

Use database defaults whenever possible.

Avoid Java-side initialization that conflicts with schema.

---

# Business Logic

Do NOT implement

- Calculations
- Validation
- Service calls
- Event publishing
- Repository access

Entities should remain persistence models.

---

# Performance

Avoid unnecessary relationships.

Prefer IDs over object graphs when relationship is not required.

Always use

```
LAZY
```

loading.

---

# Imports

Use only required imports.

Avoid wildcard imports.

---

# Code Quality Rules

Entity should

- Represent one table
- Have one responsibility
- Follow naming conventions
- Match schema exactly

---

# AI Self Validation

Before returning generated code verify

- Correct table name
- Correct column mappings
- Primary key exists
- Relationships correct
- LAZY fetch used
- Enum mapped as STRING
- Audit fields included if applicable
- No business logic
- No repository usage
- No service dependency
- Imports optimized

---

# Expected Output

Generate

1. Entity class
2. Imports
3. JPA annotations
4. Relationship mappings
5. Audit fields
6. Enum mappings
7. JavaDoc (if project uses it)

Do not generate

- Repository
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
Generate Course entity
```

**Expected Output**

Generate

```
Course.java
```

that

- matches the database schema
- follows JPA best practices
- follows project coding guidelines
- contains proper relationship mappings
- is production-ready
```