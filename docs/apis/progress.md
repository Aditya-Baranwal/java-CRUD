# Progress API

This document defines all Progress-related APIs exposed by the Learning Management System (LMS).

---

# Resource

```
/api/v1/progress
```

---

# Authorization

| Operation | ADMIN | INSTRUCTOR | USER |
|-----------|:-----:|:----------:|:----:|
| Get Progress | ✅ | ✅ | ✅ (Own Progress) |
| List Progress | ✅ | ✅ | ✅ (Own Progress) |
| Update Progress | ❌ | ❌ | ✅ (Own Progress) |

---

# Update Progress

Updates the learning progress of a lesson for a user.

A user can update the lesson status only for lessons belonging to courses in which they are enrolled.

## Endpoint

```http
PUT /progress/{progressId}
```

## Request Headers

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

## Path Parameters

| Name | Type | Description |
|------|------|-------------|
| progressId | Long | Progress Identifier |

## Validation

| Field | Validation |
|--------|------------|
| lessonStatus | Required, Must be STARTED or FINISHED |
| lessonId | Must belong to enrolled course |
| User | Can update only own progress |

## Request Body

```json
{
    "lessonStatus": "STARTED"
}
```

## Success Response

**HTTP Status**

```
200 OK
```

```json
{
    "message": "Progress updated successfully",
    "data": {
        "progressId": 1001,
        "lessonId": 10,
        "userId": 25,
        "lessonStatus": "STARTED",
        "startedAt": "2026-08-02T10:30:00Z",
        "completedAt": null
    }
}
```

## Business Rules

- UNSTARTED → STARTED
- STARTED → FINISHED
- FINISHED cannot transition back to STARTED.
- `startedAt` is populated when status becomes STARTED.
- `completedAt` is populated when status becomes FINISHED.
- Completing the final lesson automatically updates the enrollment status to `COMPLETE`.

## Possible Errors

| Status | Error Code | Description |
|---------|------------|-------------|
| 400 | PROGRESS_001 | Invalid lesson status |
| 403 | AUTH_001 | Unauthorized |
| 404 | PROGRESS_002 | Progress not found |
| 404 | LESSON_001 | Lesson not found |
| 409 | PROGRESS_003 | Invalid status transition |
| 409 | ENROLLMENT_001 | User is not enrolled in the course |

---

# Get Progress

Returns a progress record.

## Endpoint

```http
GET /progress/{progressId}
```

## Path Parameters

| Name | Type | Description |
|------|------|-------------|
| progressId | Long | Progress Identifier |

## Success Response

**HTTP Status**

```
200 OK
```

```json
{
    "message": "Progress fetched successfully",
    "data": {
        "progressId": 1001,
        "lessonId": 10,
        "lessonTitle": "Introduction to Spring",
        "userId": 25,
        "lessonStatus": "STARTED",
        "startedAt": "2026-08-02T10:30:00Z",
        "completedAt": null
    }
}
```

## Possible Errors

| Status | Error Code | Description |
|---------|------------|-------------|
| 404 | PROGRESS_002 | Progress not found |

---

# List Progress

Returns the lesson progress for a user.

## Endpoint

```http
GET /progress
```

## Query Parameters

| Parameter | Type | Required | Default | Description |
|------------|------|----------|---------|-------------|
| userId | Long | Yes | - | User whose progress is fetched |
| courseId | Long | No | - | Filter progress by course |
| moduleId | Long | No | - | Filter progress by module |
| lessonStatus | Enum | No | - | UNSTARTED / STARTED / FINISHED |
| pageNo | Integer | Yes | 1 | Page number |
| pageSize | Integer | Yes | 10 | Page size |
| sortBy | String | No | lessonId | Sort field |
| sortOrder | String | No | asc | asc / desc |

## Example

```http
GET /progress?userId=25&pageNo=1&pageSize=10
```

```http
GET /progress?userId=25&courseId=10
```

```http
GET /progress?userId=25&lessonStatus=FINISHED
```

## Success Response

**HTTP Status**

```
200 OK
```

```json
{
    "message": "Progress fetched successfully",
    "data": [
        {
            "progressId": 1001,
            "lessonId": 10,
            "lessonTitle": "Introduction to Spring",
            "moduleId": 5,
            "courseId": 1,
            "lessonStatus": "FINISHED",
            "startedAt": "2026-08-02T10:30:00Z",
            "completedAt": "2026-08-02T11:15:00Z"
        },
        {
            "progressId": 1002,
            "lessonId": 11,
            "lessonTitle": "Dependency Injection",
            "moduleId": 5,
            "courseId": 1,
            "lessonStatus": "STARTED",
            "startedAt": "2026-08-02T11:20:00Z",
            "completedAt": null
        }
    ],
    "page": 1,
    "size": 10,
    "total": 35
}
```

---

# Error Codes

| Code | Description |
|------|-------------|
| PROGRESS_001 | Invalid lesson status |
| PROGRESS_002 | Progress not found |
| PROGRESS_003 | Invalid status transition |
| LESSON_001 | Lesson not found |
| ENROLLMENT_001 | User is not enrolled in the course |
| AUTH_001 | Unauthorized |

---

# State Transition

| Current Status | Allowed Next Status |
|----------------|---------------------|
| UNSTARTED | STARTED |
| STARTED | FINISHED |
| FINISHED | — |

---

# Design Decisions

- A progress record is created automatically when a user enrolls in a course.
- One progress record exists for each `(userId, lessonId)` pair.
- `(user_id, lesson_id)` is enforced as a unique constraint.
- Users can update only their own progress.
- Progress can move only in the forward direction.
- Completing the last lesson automatically marks the enrollment as `COMPLETE`.
- `startedAt` and `completedAt` are maintained automatically by the system.
- Pagination, filtering, and sorting are supported for list APIs.
- Responses follow the standard API response format.