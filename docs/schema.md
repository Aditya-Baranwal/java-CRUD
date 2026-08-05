# Database Schema

This document defines the physical database schema for the Learning Management System (LMS). It serves as the source of truth for table structures, constraints, indexes, and enumerations.

---

# Table of Contents

- Naming Conventions
- Tables
    - User
    - Course
    - Module
    - Lesson
    - Enrollment
    - Progress
- Foreign Keys
- Primary Keys
- Unique Constraints
- Indexes
- Enumerations

---

# Naming Conventions

## Tables

- Singular noun
- Snake case

Examples

```text
user
course
module
lesson
enrollment
progress
```

---

## Columns

- Snake case
- Lowercase

Examples

```text
created_at
updated_at
course_id
lesson_id
password_hash
```

---

## Primary Key

Every table uses

```text
id
```

Type

```text
BIGINT
```

---

## Foreign Key

Foreign keys follow

```text
<parent_table>_id
```

Examples

```text
course_id

module_id

lesson_id

user_id
```

---

## Boolean Columns

Boolean columns start with

```text
is_
```

Examples

```text
is_active
is_deleted
```

---

## Timestamp Columns

Timestamp columns end with

```text
_at
```

Examples

```text
created_at
updated_at
started_at
completed_at
```

---

# Tables

---

## User

### Description

Stores all users of the platform.

| Column | Type | Default | Nullable | Constraint |
|---------|------|----------|----------|------------|
| id | BIGINT | AUTO_INCREMENT | No | PK |
| name | VARCHAR(100) | - | No | |
| dob | DATE | - | No | |
| mobile_no | VARCHAR(10) | - | No | UK |
| email_id | VARCHAR(100) | - | No | UK |
| password_hash | VARCHAR(255) | - | No | |
| role | USER_ROLE | - | No | |
| created_at | TIMESTAMP | CURRENT_TIMESTAMP | No | |

---

## Course

### Description

Stores all courses available in the system.

| Column | Type | Default | Nullable | Constraint |
|---------|------|----------|----------|------------|
| id | BIGINT | AUTO_INCREMENT | No | PK |
| title | VARCHAR(100) | - | No | |
| description | VARCHAR(200) | - | No | |
| instructor_id | BIGINT | - | No | FK |
| tags | JSONB | '[]' | No | |
| is_active | BOOLEAN | TRUE | No | |
| created_at | TIMESTAMP | CURRENT_TIMESTAMP | No | |

---

## Module

### Description

Represents logical sections inside a course.

| Column | Type | Default | Nullable | Constraint |
|---------|------|----------|----------|------------|
| id | BIGINT | AUTO_INCREMENT | No | PK |
| course_id | BIGINT | - | No | FK |
| title | VARCHAR(50) | - | No | |
| description | VARCHAR(100) | - | Yes | |
| sequence | INT | - | No | |
| is_active | BOOLEAN | TRUE | No | |
| created_at | TIMESTAMP | CURRENT_TIMESTAMP | No | |

---

## Lesson

### Description

Represents learning material inside a module.

| Column | Type | Default | Nullable | Constraint |
|---------|------|----------|----------|------------|
| id | BIGINT | AUTO_INCREMENT | No | PK |
| module_id | BIGINT | - | No | FK |
| content_type | CONTENT_TYPE | - | No | |
| content_link | VARCHAR(255) | - | No | |
| sequence | INT | - | No | |
| is_active | BOOLEAN | TRUE | No | |
| created_at | TIMESTAMP | CURRENT_TIMESTAMP | No | |

---

## Enrollment

### Description

Represents a user's enrollment into a course.

| Column | Type | Default | Nullable | Constraint |
|---------|------|----------|----------|------------|
| id | BIGINT | AUTO_INCREMENT | No | PK |
| user_id | BIGINT | - | No | FK |
| course_id | BIGINT | - | No | FK |
| course_status | COURSE_STATUS | INCOMPLETE | No | |
| enrolled_at | TIMESTAMP | CURRENT_TIMESTAMP | No | |

---

## Progress

### Description

Tracks lesson completion for every enrolled user.

| Column | Type | Default | Nullable | Constraint |
|---------|------|----------|----------|------------|
| id | BIGINT | AUTO_INCREMENT | No | PK |
| user_id | BIGINT | - | No | FK |
| lesson_id | BIGINT | - | No | FK |
| lesson_status | LESSON_STATUS | UNSTARTED | No | |
| started_at | TIMESTAMP | NULL | Yes | |
| completed_at | TIMESTAMP | NULL | Yes | |

---

# Relationships

| Parent | Child | Cardinality |
|---------|-------|-------------|
| User | Course | One-To-Many |
| User | Enrollment | One-To-Many |
| User | Progress | One-To-Many |
| Course | Module | One-To-Many |
| Course | Enrollment | One-To-Many |
| Module | Lesson | One-To-Many |
| Lesson | Progress | One-To-Many |

---

# Primary Keys

| Table | Primary Key |
|---------|-------------|
| User | id |
| Course | id |
| Module | id |
| Lesson | id |
| Enrollment | id |
| Progress | id |

---

# Foreign Keys

| Table | Column | References |
|---------|---------|------------|
| course | instructor_id | user(id) |
| module | course_id | course(id) |
| lesson | module_id | module(id) |
| enrollment | user_id | user(id) |
| enrollment | course_id | course(id) |
| progress | user_id | user(id) |
| progress | lesson_id | lesson(id) |

---

# Unique Constraints

## User

```sql
UNIQUE(email_id)

UNIQUE(mobile_no)
```

---

## Enrollment

```sql
UNIQUE(user_id, course_id)
```

---

## Progress

```sql
UNIQUE(user_id, lesson_id)
```

---

## Module

Ensures module ordering is unique within a course.

```sql
UNIQUE(course_id, sequence)
```

---

## Lesson

Ensures lesson ordering is unique within a module.

```sql
UNIQUE(module_id, sequence)
```

---

# Indexes

## User

```sql
CREATE UNIQUE INDEX uk_user_email
ON user(email_id);

CREATE UNIQUE INDEX uk_user_mobile
ON user(mobile_no);
```

---

## Course

```sql
CREATE INDEX idx_course_instructor
ON course(instructor_id);

CREATE INDEX idx_course_active
ON course(is_active);
```

---

## Module

```sql
CREATE INDEX idx_module_course
ON module(course_id);

CREATE UNIQUE INDEX uk_module_sequence
ON module(course_id, sequence);
```

---

## Lesson

```sql
CREATE INDEX idx_lesson_module
ON lesson(module_id);

CREATE UNIQUE INDEX uk_lesson_sequence
ON lesson(module_id, sequence);
```

---

## Enrollment

```sql
CREATE UNIQUE INDEX uk_enrollment
ON enrollment(user_id, course_id);

CREATE INDEX idx_enrollment_user
ON enrollment(user_id);

CREATE INDEX idx_enrollment_course
ON enrollment(course_id);

CREATE INDEX idx_enrollment_status
ON enrollment(course_status);
```

---

## Progress

```sql
CREATE UNIQUE INDEX uk_progress
ON progress(user_id, lesson_id);

CREATE INDEX idx_progress_user
ON progress(user_id);

CREATE INDEX idx_progress_lesson
ON progress(lesson_id);

CREATE INDEX idx_progress_status
ON progress(lesson_status);
```

---

# Enumerations

## USER_ROLE

| Value | Description |
|--------|-------------|
| ADMIN | Platform administrator |
| INSTRUCTOR | Course instructor |
| USER | Student |

---

## COURSE_STATUS

| Value | Description |
|--------|-------------|
| INCOMPLETE | Course is in progress |
| COMPLETE | Course completed |

---

## LESSON_STATUS

| Value | Description |
|--------|-------------|
| UNSTARTED | Lesson not started |
| STARTED | Lesson started |
| FINISHED | Lesson completed |

---

## CONTENT_TYPE

| Value | Description |
|--------|-------------|
| MP3 | Audio content |
| MP4 | Video content |
| PDF | PDF document |
| TEXT | Text content |

---

# Default Values

| Column | Default |
|---------|----------|
| course.is_active | TRUE |
| module.is_active | TRUE |
| lesson.is_active | TRUE |
| course.tags | [] |
| enrollment.course_status | INCOMPLETE |
| progress.lesson_status | UNSTARTED |
| created_at | CURRENT_TIMESTAMP |

---

# Database Constraints

- Every table uses a surrogate `BIGINT` primary key.
- Foreign keys enforce referential integrity.
- `(user_id, course_id)` must be unique to prevent duplicate enrollments.
- `(user_id, lesson_id)` must be unique to prevent duplicate progress records.
- `(course_id, sequence)` ensures module ordering is unique within a course.
- `(module_id, sequence)` ensures lesson ordering is unique within a module.
- Course, Module, and Lesson implement soft delete using `is_active`.
- Enrollment and Progress are immutable business records and are never soft deleted.
- Audit timestamps are maintained for all business entities.