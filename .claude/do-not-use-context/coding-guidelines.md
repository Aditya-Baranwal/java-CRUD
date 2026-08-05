# API Specification Context

> Purpose:
>
> This document provides AI agents and developers with a high-level understanding
> of the APIs exposed by the system.
>
> It intentionally does **not** describe complete request/response payloads.
> Those are documented in **docs/api.md**.
>
> This document explains the purpose, ownership, behavior, constraints,
> architectural expectations and business rules associated with APIs.

---

# 1. API Philosophy

The service follows REST architecture principles.

## Design Principles

- Resource-oriented APIs
- Stateless communication
- JSON request/response
- HTTP status codes are authoritative
- Idempotent operations wherever applicable
- Predictable URI naming
- Backward compatible changes
- API-first development

---

# 2. API Versioning

Current Version

```
/api/v1
```

Future versions

```
/api/v2
```

Rules

- Never introduce breaking changes inside same version.
- New optional fields are allowed.
- Existing fields must never change semantics.
- Deprecated APIs should remain available until sunset period.

---

# 3. API Groups

## Authentication

Responsible for

- Login
- Registration
- Token Refresh
- Logout

---

## User

Responsible for

- User Profile
- User Management
- Role Management

---

## Course

Responsible for

- Create Course
- Update Course
- Delete Course
- Get Course
- List Courses

---

## Module

Responsible for

- Create Module
- Update Module
- Delete Module
- List Modules

---

## Lesson

Responsible for

- Lesson CRUD
- Lesson Ordering

---

## Enrollment

Responsible for

- Enroll User
- List Enrollments

---

## Progress

Responsible for

- Update Lesson Progress
- Fetch Learning Progress

---

# 4. Resource Ownership

| Resource | Owner |
|----------|-------|
| User | User Service |
| Course | Course Service |
| Module | Course Service |
| Lesson | Course Service |
| Enrollment | Enrollment Service |
| Progress | Progress Service |

---

# 5. URI Standards

Resources use plural nouns.

Good

```
GET /courses
GET /courses/{id}
POST /courses
PUT /courses/{id}
DELETE /courses/{id}
```

Avoid

```
/createCourse
/getCourses
/courseList
```

---

# 6. HTTP Method Guidelines

GET

- Read data
- Never modifies state

POST

- Create resource

PUT

- Complete update
- Idempotent

PATCH

- Partial update

DELETE

- Soft delete whenever possible

---

# 7. Naming Convention

URI

```
/courses
/modules
/lessons
```

JSON

camelCase

```
courseTitle
courseDescription
createdAt
```

Enum

UPPER_CASE

```
ACTIVE
INACTIVE
STARTED
FINISHED
```

---

# 8. Authentication

Authentication

JWT Bearer Token

```
Authorization:
Bearer <jwt-token>
```

Public APIs

- Login
- Register
- Health Check

All remaining APIs require authentication.

---

# 9. Authorization

## Admin

Can access every endpoint.

## Instructor

Can

- Create Course
- Update Own Course
- Manage Modules
- Manage Lessons

Cannot

- Modify another instructor's course

## Student

Can

- Enroll
- View Courses
- Update Progress

Cannot

- Create Courses

---

# 10. API Behaviour

## GET APIs

Must never change server state.

Must be cache friendly.

---

## POST APIs

Should return

```
201 Created
```

Location header is recommended.

---

## PUT APIs

Must be idempotent.

Calling same request multiple times should produce identical result.

---

## DELETE APIs

Prefer soft delete.

Data should remain recoverable unless business explicitly requires hard delete.

---

# 11. Pagination

Every listing endpoint must support

```
page
size
sort
```

Response should contain

```
page
size
totalRecords
totalPages
```

Maximum page size

```
100
```

---

# 12. Filtering

Filtering should use query parameters.

Example

```
GET /courses?active=true
GET /courses?tag=java
```

---

# 13. Sorting

Example

```
sort=createdAt,desc
sort=title,asc
```

---

# 14. Standard Response Format

Successful Response

```json
{
  "message": "",
  "data": {}
}
```

List Response

```json
{
  "message": "",
  "data": [],
  "page": 1,
  "size": 10,
  "total": 100
}
```

---

# 15. Standard Error Format

```json
{
    "code": "COURSE_NOT_FOUND",
    "message": "Course does not exist",
    "traceId": "ab123xyz",
    "errors": []
}
```

---

# 16. Common Error Codes

Validation Error

```
400
```

Authentication Failed

```
401
```

Permission Denied

```
403
```

Resource Missing

```
404
```

Conflict

```
409
```

Unexpected Error

```
500
```

---

# 17. Validation Rules

API layer performs

- Required field validation
- Field length validation
- Enum validation
- Request format validation

Business validation belongs in service layer.

---

# 18. Idempotency

Mandatory

- PUT
- DELETE

Recommended

Enrollment APIs

Should support

```
Idempotency-Key
```

to avoid duplicate enrollments.

---

# 19. Transactions

Database transaction required for

- Create Course
- Update Course
- Delete Course
- Create Enrollment
- Update Progress

Read APIs should remain read-only.

---

# 20. Event Publishing

The following operations publish domain events.

## Enrollment Created

Publishes

```
EnrollmentCreatedEvent
```

---

## Course Completed

Publishes

```
CourseCompletedEvent
```

---

## Lesson Finished

Publishes

```
LessonCompletedEvent
```

---

# 21. Performance Expectations

Read APIs

P95

```
< 200 ms
```

Write APIs

P95

```
< 500 ms
```

Pagination mandatory for large datasets.

Avoid N+1 database queries.

---

# 22. Observability

Every request should produce

- Trace Id
- Request Id
- Correlation Id

Metrics

- Request Count
- Success Count
- Failure Count
- Latency
- Database Calls

Logs

- Request Start
- Request End
- Exception
- Validation Failure

---

# 23. Security

Input Validation

Enabled

Output Encoding

Enabled

Authentication

JWT

Authorization

RBAC

Sensitive information

Never returned in APIs.

Passwords

Never logged.

---

# 24. API Documentation

Detailed endpoint definitions are maintained separately.

```
docs/api.md
```

Contains

- URI
- Request Body
- Response Body
- Query Parameters
- Path Parameters
- Validation Rules
- Error Responses
- Examples

---

# 25. Related Documents

| Document | Purpose |
|-----------|---------|
| docs/api.md | Complete endpoint documentation |
| docs/database.md | Database schema |
| context/domain.md | Domain model |
| context/business-rules.md | Business rules |
| context/security.md | Security guidelines |
| context/error-handling.md | Error handling |
| context/performance.md | Performance expectations |
| context/architecture.md | System architecture |

---

# 26. AI Implementation Guidelines

When implementing APIs, the AI agent must

- Follow REST conventions.
- Reuse existing DTOs whenever possible.
- Never bypass service layer.
- Never expose database entities directly.
- Maintain backward compatibility.
- Validate all incoming requests.
- Return standard response format.
- Use existing exception hierarchy.
- Follow project package conventions.
- Add unit tests for every new endpoint.
- Update docs/api.md after introducing any API.

---

# 27. Future Enhancements

Planned API capabilities

- Certificates
- Course Reviews
- Wishlist
- Recommendations
- Notifications
- Search
- Bulk Enrollment
- Audit APIs
- Admin Dashboard APIs