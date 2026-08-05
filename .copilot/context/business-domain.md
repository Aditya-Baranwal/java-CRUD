# Business Domain

This document defines the business domain for the Learning Management System (LMS).

It provides domain terminology, business rules, and workflows used by the application.

Detailed API, database, and architecture specifications are available under the `docs/` directory.

---

# Source of Truth

Refer to the following documents for detailed specifications.

- docs/requirements.md
- docs/database.md
- docs/schema.md
- docs/api.md

If this document conflicts with any document in `docs/`, the document in `docs/` takes precedence.

---

# Business Goal

The Learning Management System enables instructors to create online courses and allows learners to enroll, consume learning content, and track their learning progress.

---

# Actors

## Administrator

Responsibilities

- Manage users
- Manage instructors
- Manage courses
- Manage learning content
- View system reports

---

## Instructor

Responsibilities

- Create courses
- Update courses
- Organize modules
- Create lessons
- Publish learning content

---

## Learner

Responsibilities

- Browse courses
- Enroll in courses
- Consume lessons
- Track learning progress
- Complete courses

---

# Core Domain Entities

## User

Represents a person using the LMS.

Roles

- ADMIN
- INSTRUCTOR
- USER

---

## Course

Represents a learning program.

A course

- is created by one instructor
- contains one or more modules
- can be active or inactive
- can be enrolled by many learners

---

## Module

Represents a logical section of a course.

A module

- belongs to one course
- contains one or more lessons
- has a display sequence

---

## Lesson

Represents a single learning unit.

A lesson

- belongs to one module
- has a content type
- has a display sequence

Supported content includes

- Video
- Audio
- PDF
- Text

---

## Enrollment

Represents a learner joining a course.

An enrollment

- belongs to one learner
- belongs to one course
- tracks overall course completion

A learner can enroll in a course only once.

---

## Progress

Represents a learner's progress for a lesson.

Progress

- belongs to one learner
- belongs to one lesson
- tracks lesson completion status

Each learner has at most one progress record per lesson.

---

# Business Relationships

```
User (Instructor)
        │
        ▼
     Course
        │
        ▼
     Module
        │
        ▼
     Lesson

User (Learner)
        │
        ▼
 Enrollment
        │
        ▼
    Progress
```

---

# Business Rules

## Course Rules

- Every course must have an instructor.
- A course must contain at least one module before publication.
- Inactive courses cannot accept new enrollments.
- Soft delete is implemented by marking a course as inactive.

---

## Module Rules

- Every module belongs to exactly one course.
- Modules are displayed according to their sequence.
- Soft delete is implemented by marking a module as inactive.

---

## Lesson Rules

- Every lesson belongs to exactly one module.
- Lessons are displayed according to their sequence.
- A lesson must have a supported content type.
- Soft delete is implemented by marking a lesson as inactive.

---

## Enrollment Rules

- A learner may enroll only once per course.
- Enrollment creates an initial course status of `INCOMPLETE`.
- Only enrolled learners may access course lessons.

---

## Progress Rules

- Progress is tracked per lesson.
- Lesson status changes from

```
UNSTARTED
    ↓
STARTED
    ↓
FINISHED
```

- Completing every lesson marks the course as `COMPLETE`.

---

# Business Workflow

## Course Creation

```
Instructor

    ↓

Create Course

    ↓

Add Modules

    ↓

Add Lessons

    ↓

Publish Course
```

---

## Course Enrollment

```
Learner

    ↓

Browse Courses

    ↓

Enroll

    ↓

Enrollment Created
```

---

## Learning Journey

```
Enrollment

    ↓

Start Lesson

    ↓

Update Progress

    ↓

Finish Lesson

    ↓

Repeat

    ↓

Course Completed
```

---

# Business Terminology

| Term | Meaning |
|------|---------|
| Course | Complete learning program |
| Module | Section within a course |
| Lesson | Individual learning unit |
| Enrollment | Learner registered for a course |
| Progress | Learner's completion state for a lesson |
| Instructor | User who owns courses |
| Learner | User consuming courses |

---

# AI Context Usage

This document describes **what** the business does.

For implementation details, refer to:

Architecture

- context/architectural-spec.md

Engineering Standards

- context/coding-standards.md

Detailed Documentation

- docs/
