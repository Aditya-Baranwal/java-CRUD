# API Design

This document describes the API conventions, standards, authentication, and response formats used by the Learning Management System (LMS).

Individual endpoint specifications are available under the `apis/` directory.

---

# Table of Contents

- API Versioning
- Base URL
- Authentication
- Content Negotiation
- HTTP Methods
- Resource Naming Convention
- Request Standards
- Response Standards
- Error Response
- Pagination
- Sorting
- Filtering
- HTTP Status Codes
- Authorization Model
- API Specifications
- Design Principles

---

# API Versioning

Current version

```
/api/v1
```

Breaking API changes will be introduced using a new major version.

Example

```
/api/v2/courses
```

---

# Base URL

```
/api/v1
```

---

# Authentication

All APIs except authentication endpoints require a JWT Access Token.

```
Authorization: Bearer <JWT_TOKEN>
```

---

# Content Negotiation

Request

```
Content-Type: application/json
```

Response

```
Accept: application/json
```

---

# HTTP Methods

| Method | Usage                    |
|--------|--------------------------|
| GET    | Retrieve resource(s)     |
| POST   | Create resource          |
| PUT    | Update existing resource |
| DELETE | Soft delete resource     |

---

# Resource Naming Convention

The API follows REST principles.

Resources use plural nouns.

Examples

```
/courses
/modules
/lessons
/enrollments
/progress
```

Resources are never nested.

Good

```
GET /modules?courseId=1

GET /lessons?moduleId=5
```

Avoid

```
GET /courses/1/modules

GET /modules/5/lessons
```

Relationships are represented using query parameters instead of nested URLs.

---

# Request Standards

## Path Parameters

Used to identify a resource.

Example

```
GET /courses/{courseId}
```

---

## Query Parameters

Used for

- filtering
- sorting
- pagination
- optional expansion

Example

```
GET /courses?pageNo=1&pageSize=20

GET /modules?courseId=5

GET /lessons?moduleId=2&userId=10
```

---

## Request Body

Used only for

- POST
- PUT

GET and DELETE APIs should not accept request bodies.

---

# Standard Response Format

## Success Response

```json
{
    "message": "Operation completed successfully",
    "data": {},
    "timestamp": "2026-08-02T18:30:00Z"
}
```

---

## Paginated Response

```json
{
    "message": "Success",
    "data": [],
    "page": 1,
    "size": 20,
    "total": 150,
    "timestamp": "2026-08-02T18:30:00Z"
}
```

---

## Error Response

```json
{
    "message": "Validation Failed",
    "errorCode": "COURSE_001",
    "errors": [
        {
            "field": "courseTitle",
            "message": "Course title is mandatory"
        }
    ],
    "timestamp": "2026-08-02T18:30:00Z"
}
```

---

# Pagination

All list APIs support pagination.

| Parameter | Required | Default |
|-----------|----------|---------|
| pageNo    | Yes      | 1       |
| pageSize  | Yes      | 10      |

---

# Sorting

All list APIs support sorting.

| Parameter | Default   |
|-----------|-----------|
| sortBy    | createdAt |
| sortOrder | asc       |

Example

```
GET /courses?sortBy=createdAt&sortOrder=desc
```

---

# Filtering

Filtering is implemented using query parameters.

Examples

```
GET /courses?active=true

GET /modules?courseId=10

GET /lessons?moduleId=5

GET /progress?courseId=2&lessonStatus=FINISHED
```

---

# HTTP Status Codes

| Code | Meaning               |
|------|-----------------------|
| 200  | Success               |
| 201  | Created               |
| 204  | No Content            |
| 400  | Bad Request           |
| 401  | Unauthorized          |
| 403  | Forbidden             |
| 404  | Not Found             |
| 409  | Conflict              |
| 500  | Internal Server Error |

---

# Authorization Model

| Resource    | ADMIN | INSTRUCTOR |       USER       |
|-------------|:-----:|:----------:|:----------------:|
| Courses     | CRUD  | CRUD (Own) |       Read       |
| Modules     | CRUD  | CRUD (Own) |       Read       |
| Lessons     | CRUD  | CRUD (Own) |       Read       |
| Enrollments | Read  |    Read    | Create, Read Own |
| Progress    | Read  |    Read    |    Update Own    |

---

# API Specifications

Detailed API documentation is available in the following files.

| Resource    | Specification         |
|-------------|-----------------------|
| Courses     | `apis/courses.md`     |
| Modules     | `apis/modules.md`     |
| Lessons     | `apis/lessons.md`     |
| Enrollments | `apis/enrollments.md` |
| Progress    | `apis/progress.md`    |

---

# API Design Principles

The APIs follow the following design principles.

- RESTful resource-oriented APIs.
- Plural resource names.
- No nested resource URLs.
- Relationships represented using query parameters.
- Stateless APIs.
- JWT-based authentication.
- Soft delete using `is_active`.
- Consistent request and response structures.
- Standard HTTP status codes.
- Standardized error codes.
- Pagination for all list APIs.
- Sorting and filtering through query parameters.
- URI versioning (`/api/v1`).