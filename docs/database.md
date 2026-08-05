# Database Design

This document describes the logical database design for the Learning Management System (LMS).

---

# Table of Contents

- Database Selection
- Design Principles
- Entity Relationship Diagram
- Relationships
- Constraints
- Indexing Strategy
- Soft Delete Strategy
- Enumerations
- Design Decisions

---

# Database Selection

## Database

**PostgreSQL**

### Why PostgreSQL?

- ACID compliant transactions
- Strong referential integrity
- JSONB support for flexible metadata
- Excellent indexing support
- Mature ecosystem
- High concurrency support

---

# Design Principles

The database design follows these principles:

- Third Normal Form (3NF)
- Surrogate Primary Keys (`BIGINT`)
- Foreign Key constraints
- Soft delete for business entities
- Audit columns on every entity
- Immutable primary keys
- Database-level referential integrity

---

# Entity Relationship Diagram

```text
                +-------------+
                |    User     |
                +-------------+
                 |        |
                 |        |
                 |        +----------------------+
                 |                               |
                 |                               |
                 v                               v
        +----------------+             +------------------+
        |   Enrollment   |             |      Course      |
        +----------------+             +------------------+
                 |                               |
                 |                               |
                 |                               |
                 |                               |
                 |                               v
                 |                     +------------------+
                 |                     |      Module      |
                 |                     +------------------+
                 |                               |
                 |                               |
                 |                               v
                 |                     +------------------+
                 |                     |      Lesson      |
                 |                     +------------------+
                 |                               |
                 |                               |
                 +---------------+---------------+
                                 |
                                 v
                         +----------------+
                         |    Progress    |
                         +----------------+
```

---

# Relationships

| Parent | Child | Cardinality |
|---------|--------|-------------|
| User | Enrollment | One-To-Many |
| User | Course (Instructor) | One-To-Many |
| Course | Module | One-To-Many |
| Module | Lesson | One-To-Many |
| User | Progress | One-To-Many |
| Lesson | Progress | One-To-Many |
| Course | Enrollment | One-To-Many |

---

# Referential Integrity

Foreign Keys enforce consistency between parent and child entities.

| Parent | Child |
|---------|-------|
| User | Course |
| User | Enrollment |
| User | Progress |
| Course | Module |
| Course | Enrollment |
| Module | Lesson |
| Lesson | Progress |

---

# Indexing Strategy

## Primary Keys

Every table uses

- BIGINT
- Auto Increment
- Clustered Primary Key

---

## Secondary Indexes

### User

- email_id
- mobile_no

### Course

- instructor_id
- is_active

### Module

- course_id
- (course_id, sequence)

### Lesson

- module_id
- (module_id, sequence)

### Enrollment

- user_id
- course_id
- (user_id, course_id)

### Progress

- user_id
- lesson_id
- (user_id, lesson_id)

---

# Soft Delete Strategy

Soft delete is supported for:

- Course
- Module
- Lesson

using

```
is_active
```

Enrollment and Progress are never soft deleted.

---

# Enumerations

- UserRole
- CourseStatus
- LessonStatus
- ContentType

(Reference: `schema.md`)

---

# Design Decisions

- PostgreSQL selected for transactional consistency.
- JSONB stores flexible course tags.
- Composite unique indexes prevent duplicate enrollments.
- Ordering is maintained using sequence columns.
- All business entities contain audit fields.
- Foreign Keys enforce referential integrity.
- Soft delete preserves historical information.