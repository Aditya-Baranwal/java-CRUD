# Courses API

This document describes the current API contract for course endpoints.

---

# Resource

```text
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

# Shared course response shapes

## Plain course shape

Used by:
- create course
- update course
- get course when `progress=false` or omitted
- list courses when `progress=false` or omitted

```json
{
  "courseId": 1,
  "courseTitle": "Java Spring Boot",
  "courseDescription": "Complete Spring Boot course",
  "courseTags": ["java", "spring"],
  "courseStatus": "PUBLISHED",
  "instructorId": 101,
  "canEnrollment": true,
  "totalModuleCount": 4,
  "modules": [],
  "createdAt": "2026-08-02T10:30:00Z"
}
```

## Course + progress shape

Used only when `progress=true`.

```json
{
  "courseId": 1,
  "courseTitle": "Java Spring Boot",
  "courseDescription": "Complete Spring Boot course",
  "courseTags": ["java", "spring"],
  "courseStatus": "PUBLISHED",
  "instructorId": 101,
  "canEnrollment": true,
  "userId": 25,
  "isCourseCompleted": false,
  "completedModuleCount": 2,
  "totalModuleCount": 4,
  "modules": [],
  "createdAt": "2026-08-02T10:30:00Z"
}
```

### Progress field semantics

- `userId`: echoes the queried user id.
- `totalModuleCount`: total number of **active** modules in the course.
- `completedModuleCount`: number of active modules completed by `userId`.
- `isCourseCompleted`: derived from enrollment source of truth: `enrollments.course_completion_status == COMPLETE`.
- If the user is not enrolled:
  - `isCourseCompleted: null`
  - `completedModuleCount: null`
  - `totalModuleCount`: still populated
- If the user is enrolled and the course has zero active modules:
  - `isCourseCompleted: false`
- When `includeModules=true`, nested modules remain in the plain module summary shape.

---

# Create Course

Creates a new course.

## Endpoint

```http
POST /courses
```

## Request Body

```json
{
  "courseTitle": "Java Spring Boot",
  "courseDescription": "Complete Spring Boot course",
  "courseTags": ["java", "spring"],
  "instructorId": 101
}
```

## Success Response

**HTTP Status**

```text
201 Created
```

---

# Update Course

Updates an existing course.

## Endpoint

```http
PUT /courses/{courseId}
```

## Success Response

**HTTP Status**

```text
200 OK
```

---

# Get Course

Returns a course by identifier.

## Endpoint

```http
GET /courses/{courseId}
```

## Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| includeModules | Boolean | No | false | Include course modules |
| userId | Long | No* | - | Required when `progress=true`; selects which user's completion details to include |
| progress | Boolean | No | false | When `true`, returns the course + progress shape |

\* `userId` is mandatory whenever `progress=true`.

## Example

Plain course:

```http
GET /courses/1
```

Course with progress:

```http
GET /courses/1?progress=true&userId=25
```

## Possible Errors

| Status | Error Code | Description |
|--------|------------|-------------|
| 400 | COURSE_013 | userId is mandatory when progress=true |
| 404 | COURSE_404 | Course not found |

---

# List Courses

Returns a paginated list of courses.

## Endpoint

```http
GET /courses
```

## Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| pageNo | Integer | Yes | 1 | Page number |
| pageSize | Integer | Yes | 10 | Page size |
| courseStatus | String | No | - | Filter by lifecycle status |
| userId | Long | No* | - | Required when `progress=true`; selects which user's completion details to include |
| progress | Boolean | No | false | When `true`, returns each item in the course + progress shape |
| sortBy | String | No | createdAt | Sort field |
| sortOrder | String | No | desc | `asc` / `desc` |

\* `userId` is mandatory whenever `progress=true`.

## Example

Plain course list:

```http
GET /courses?pageNo=1&pageSize=10
```

Course list with progress:

```http
GET /courses?pageNo=1&pageSize=10&progress=true&userId=25
```

## Possible Errors

| Status | Error Code | Description |
|--------|------------|-------------|
| 400 | COURSE_013 | userId is mandatory when progress=true |

---

# Delete Course

Performs a soft delete by setting:
- `courseStatus = MANUAL_UNPUBLISHED`
- `canEnrollment = false`

## Endpoint

```http
DELETE /courses/{courseId}
```

---

# Design Decisions

- Plain read responses always include `totalModuleCount` (active modules only).
- Progress read responses are enabled with `progress=true` and require `userId`.
- `isCourseCompleted` uses enrollment completion status as source of truth.
