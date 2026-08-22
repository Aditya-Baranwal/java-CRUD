# Courses API

This document defines all Course-related APIs exposed by the Learning Management System (LMS).

---

# Resource

```
/api/v1/courses
```

---

# Authorization

| Operation     | ADMIN |   INSTRUCTOR    | USER |
|---------------|:-----:|:---------------:|:----:|
| Create Course |  ✅   |       ✅        |  ❌  |
| Update Course |  ✅   | ✅ (Own Course) |  ❌  |
| Get Course    |  ✅   |       ✅        |  ✅  |
| List Courses  |  ✅   |       ✅        |  ✅  |
| Delete Course |  ✅   |       ❌        |  ❌  |

---

# Create Course

Creates a new course.

## Endpoint

```http
POST /courses
```

## Request Headers

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

## Validation

| Field             | Validation                       |
|-------------------|----------------------------------|
| courseTitle       | Required, Maximum 100 characters |
| courseDescription | Optional, Maximum 200 characters |
| instructorId      | Required, Instructor must exist  |
| courseTags        | Optional                         |

## Request Body

```json
{
    "courseTitle": "Java Spring Boot",
    "courseDescription": "Complete Spring Boot course",
    "courseTags": [
        "java",
        "spring",
        "backend"
    ],
    "instructorId": 101
}
```

## Success Response

**HTTP Status**

```
201 Created
```

```json
{
    "message": "Course created successfully",
    "data": {
        "courseId": 1,
        "courseTitle": "Java Spring Boot",
        "courseDescription": "Complete Spring Boot course",
        "courseTags": [
            "java",
            "spring",
            "backend"
        ],
        "courseStatus": "DRAFT",
        "instructorId": 101,
        "instructorName": "John Doe",
        "isActive": true,
        "createdAt": "2026-08-02T10:30:00Z"
    }
}
```

## Possible Errors

| Status | Error Code | Description               |
|--------|------------|---------------------------|
| 400    | COURSE_001 | Invalid request payload   |
| 400    | COURSE_002 | Course title is mandatory |
| 404    | USER_001   | Instructor not found      |
| 403    | AUTH_001   | Unauthorized              |

---

# Update Course

Updates an existing course.

## Endpoint

```http
PUT /courses/{courseId}
```

## Path Parameters

| Name     | Type | Description       |
|----------|------|-------------------|
| courseId | Long | Course Identifier |

## Validation

| Field             | Validation                       |
|-------------------|----------------------------------|
| courseTitle       | Optional, Maximum 100 characters |
| courseDescription | Optional, Maximum 200 characters |
| courseTags        | Optional                         |
| isActive          | Optional                         |

## Request Body

```json
{
    "courseTitle": "Advanced Spring Boot",
    "courseDescription": "Advanced concepts",
    "courseTags": [
        "spring",
        "java"
    ],
    "courseStatus": "DRAFT",
    "isActive": true
}
```

## Success Response

**HTTP Status**

```
200 OK
```

```json
{
    "message": "Course updated successfully",
    "data": {
        "courseId": 1,
        "courseTitle": "Advanced Spring Boot",
        "courseDescription": "Advanced concepts",
        "courseTags": [
            "spring",
            "java"
        ],
        "courseStatus": "DRAFT",
        "instructorId": 101,
        "instructorName": "John Doe",
        "isActive": true,
        "createdAt": "2026-08-02T10:30:00Z"
    }
}
```

## Possible Errors

| Status | Error Code | Description      |
|--------|------------|------------------|
| 400    | COURSE_001 | Invalid request  |
| 404    | COURSE_003 | Course not found |
| 403    | AUTH_001   | Unauthorized     |

---

# Get Course

Returns a course by its identifier.

## Endpoint

```http
GET /courses/{courseId}
```

## Path Parameters

| Name     | Type | Description       |
|----------|------|-------------------|
| courseId | Long | Course Identifier |

## Query Parameters

| Parameter      | Type    | Required | Default | Description            |
|----------------|---------|----------|---------|------------------------|
| includeModules | Boolean | No       | false   | Include course modules |

## Success Response

**HTTP Status**

```
200 OK
```

```json
{
    "message": "Course fetched successfully",
    "data": {
        "courseId": 1,
        "courseTitle": "Java Spring Boot",
        "courseDescription": "Complete Spring Boot course",
        "courseTags": [
            "java",
            "spring"
        ],
        "courseStatus": "DRAFT",
        "instructorId": 101,
        "instructorName": "John Doe",
        "modules": [],
        "isActive": true,
        "createdAt": "2026-08-02T10:30:00Z"
    }
}
```

## Possible Errors

| Status | Error Code | Description      |
|--------|------------|------------------|
| 404    | COURSE_003 | Course not found |

---

# List Courses

Returns a paginated list of courses.

## Endpoint

```http
GET /courses
```

## Query Parameters

| Parameter    | Type    | Required | Default   | Description                             |
|--------------|---------|----------|-----------|-----------------------------------------|
| active       | Boolean | No       | true      | Return active/inactive courses          |
| courseStatus | String  | No       | published | useful to filter course based on status |
| pageNo       | Integer | Yes      | 1         | Page number                             |
| pageSize     | Integer | Yes      | 10        | Page size                               |
| sortBy       | String  | No       | createdAt | Sort field                              |
| sortOrder    | String  | No       | desc      | asc / desc                              |

## Example

```http
GET /courses?active=true&pageNo=1&pageSize=10&sortBy=createdAt&sortOrder=desc
```

## Success Response

**HTTP Status**

```
200 OK
```

```json
{
    "message": "Courses fetched successfully",
    "data": [
        {
            "courseId": 1,
            "courseTitle": "Java",
            "courseDescription": "Java Fundamentals",
            "courseTags": [
                "java"
            ],
            "courseStatus": "DRAFT",
            "instructorId": 101,
            "instructorName": "John Doe",
            "isActive": true,
            "createdAt": "2026-08-02T10:30:00Z"
        }
    ],
    "page": 1,
    "size": 10,
    "total": 150
}
```

---

# Delete Course

Performs a soft delete by marking the course as inactive.

## Endpoint

```http
DELETE /courses/{courseId}
```

## Path Parameters

| Name     | Type | Description       |
|----------|------|-------------------|
| courseId | Long | Course Identifier |

## Behavior

- Course is **not physically deleted**.
- Updates `is_active = false`.
- Existing enrollments remain unchanged.
- Existing modules and lessons are retained.

## Success Response

**HTTP Status**

```
200 OK
```

```json
{
    "message": "Course inactivated successfully",
    "data": {}
}
```

## Possible Errors

| Status | Error Code | Description              |
|--------|------------|--------------------------|
| 404    | COURSE_003 | Course not found         |
| 409    | COURSE_004 | Course cannot be deleted |
| 403    | AUTH_001   | Unauthorized             |

---

# Error Codes

| Code       | Description               |
|------------|---------------------------|
| COURSE_001 | Invalid request           |
| COURSE_002 | Course title is mandatory |
| COURSE_003 | Course not found          |
| COURSE_004 | Course cannot be deleted  |
| USER_001   | Instructor not found      |
| AUTH_001   | Unauthorized              |

---

# Design Decisions

- Resource-oriented REST APIs.
- Soft delete is implemented using the `is_active` flag. is_active = true indicates that the courses cannot be enrolled anymore.
- had added a `courseStatus` field to manage course lifecycle, only in_active courses can be unpublished.
- Pagination is supported for all list operations.
- Sorting is supported using `sortBy` and `sortOrder`.
- Filtering is implemented using query parameters.
- Responses follow a consistent response structure.
- Authentication is JWT based.
- Only ADMIN users can delete courses.
- Instructors can manage only the courses they own.