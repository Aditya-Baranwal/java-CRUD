# Enrollments API

This document describes the current API contract for enrollment endpoints.

---

# Resource

```text
/api/v1/enrollments
```

---

# Create Enrollment

Creates a new enrollment.

## Endpoint

```http
POST /enrollments
```

## Request Body

```json
{
  "userId": 25,
  "courseId": 10
}
```

## Success Response

**HTTP Status**

```text
201 Created
```

```json
{
  "message": "Enrollment created successfully",
  "data": {
    "id": 101,
    "userId": 25,
    "courseId": 10,
    "courseTitle": "Java Spring Boot",
    "courseCompletionStatus": "INCOMPLETE",
    "enrolledAt": "2026-08-02T10:30:00Z",
    "canEnrolledStudentViewCourseContent": true,
    "courseAccessMessage": "Course is open for enrollment."
  },
  "timestamp": "2026-08-02T10:30:00Z"
}
```

---

# List Enrollments

Returns a paginated enrollment list for a user.

## Endpoint

```http
GET /enrollments
```

## Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| userId | Long | Yes | - | User whose enrollments are fetched |
| courseCompletionStatus | Enum | No | - | `COMPLETE` or `INCOMPLETE` |
| pageNo | Integer | Yes | 1 | Page number |
| pageSize | Integer | Yes | 10 | Page size |
| sortBy | String | No | enrolledAt | Sort field |
| sortOrder | String | No | desc | `asc` / `desc` |

## Success Response

**HTTP Status**

```text
200 OK
```

```json
{
  "message": "Enrollments fetched successfully",
  "data": [
    {
      "id": 101,
      "userId": 25,
      "courseId": 10,
      "courseTitle": "Java Spring Boot",
      "courseCompletionStatus": "INCOMPLETE",
      "enrolledAt": "2026-08-02T10:30:00Z",
      "canEnrolledStudentViewCourseContent": true,
      "courseAccessMessage": "Course is open for enrollment."
    }
  ],
  "page": 1,
  "size": 10,
  "total": 1,
  "timestamp": "2026-08-02T10:30:00Z"
}
```

---

# Get Enrollment

Returns a single enrollment by id.

## Endpoint

```http
GET /enrollments/{enrollmentId}
```

## Path Parameters

| Name | Type | Description |
|------|------|-------------|
| enrollmentId | Long | Enrollment identifier |

## Success Response

**HTTP Status**

```text
200 OK
```

---

# Cancel Enrollment

Cancels enrollment.

## Endpoint

```http
DELETE /enrollments/{enrollmentId}
```

## Success Response

**HTTP Status**

```text
200 OK
```

```json
{
  "message": "Enrollment cancelled successfully",
  "data": {},
  "timestamp": "2026-08-02T10:30:00Z"
}
```
