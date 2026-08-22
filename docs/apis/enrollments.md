# Enrollments API

This document defines all Enrollment-related APIs exposed by the Learning Management System (LMS).

---

# Resource

```
/api/v1/enrollments
```

---

# Authorization

| Operation                      | ADMIN | INSTRUCTOR |         USER         |
|--------------------------------|:-----:|:----------:|:--------------------:|
| Create Enrollment              |  ❌   |     ❌     |          ✅          |
| List Enrollments               |  ✅   |     ✅     | ✅ (Own Enrollments) |
| Get Enrollment                 |  ✅   |     ✅     | ✅ (Own Enrollment)  |
| Cancel Enrollment *(Optional)* |  ❌   |     ❌     |          ✅          |

---

# Create Enrollment

Enrolls a user into a course.

## Endpoint

```http
POST /enrollments
```

## Request Headers

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

## Validation

| Field             | Validation                          |
|-------------------|-------------------------------------|
| userId            | Required, User must exist           |
| courseId          | Required, Course must exist         |
| courseId          | Course must be active               |
| userId + courseId | User should not already be enrolled |

## Request Body

```json
{
    "userId": 25,
    "courseId": 10
}
```

## Success Response

**HTTP Status**

```
201 Created
```

```json
{
    "message": "Enrolled to course successfully",
    "data": {
        "id": 101,
        "userId": 25,
        "courseId": 10,
        "courseTitle": "Java Spring Boot",
        "courseCompletionStatus": "INCOMPLETE",
        "enrolledAt": "2026-08-02T10:30:00Z"
    }
}
```

## Possible Errors

| Status | Error Code     | Description           |
|--------|----------------|-----------------------|
| 400    | ENROLLMENT_001 | Invalid request       |
| 404    | USER_001       | User not found        |
| 404    | COURSE_001     | Course not found      |
| 409    | ENROLLMENT_002 | User already enrolled |
| 409    | ENROLLMENT_003 | Course is inactive    |
| 403    | AUTH_001       | Unauthorized          |

---

# Get Enrollment

Returns a single enrollment.

## Endpoint

```http
GET /enrollments/{enrollmentId}
```

## Path Parameters

| Name         | Type | Description           |
|--------------|------|-----------------------|
| enrollmentId | Long | Enrollment Identifier |

## Success Response

**HTTP Status**

```
200 OK
```

```json
{
    "message": "Enrollment fetched successfully",
    "data": {
        "id": 101,
        "userId": 25,
        "courseId": 10,
        "courseTitle": "Java Spring Boot",
        "courseCompletionStatus": "INCOMPLETE",
        "enrolledAt": "2026-08-02T10:30:00Z"
    }
}
```

## Possible Errors

| Status | Error Code     | Description          |
|--------|----------------|----------------------|
| 404    | ENROLLMENT_004 | Enrollment not found |
| 403    | AUTH_001       | Unauthorized         |

---

# List Enrollments

Returns a paginated list of enrollments.

## Endpoint

```http
GET /enrollments
```

## Query Parameters

| Parameter    | Type    | Required | Default    | Description                        |
|--------------|---------|----------|------------|------------------------------------|
| userId       | Long    | Yes      | -          | User whose enrollments are fetched |
| courseCompletionStatus | Enum    | No       | -          | COMPLETE / INCOMPLETE              |
| pageNo       | Integer | Yes      | 1          | Page number                        |
| pageSize     | Integer | Yes      | 10         | Page size                          |
| sortBy       | String  | No       | enrolledAt | Sort field                         |
| sortOrder    | String  | No       | desc       | asc / desc                         |

## Example

```http
GET /enrollments?userId=25&pageNo=1&pageSize=10
```

or

```http
GET /enrollments?userId=25&courseCompletionStatus=INCOMPLETE
```

## Success Response

**HTTP Status**

```
200 OK
```

```json
{
    "message": "Enrollments fetched successfully",
    "data": [
        {
            "id": 101,
            "courseId": 10,
            "courseTitle": "Java Spring Boot",
            "userId": 25,
            "courseCompletionStatus": "INCOMPLETE",
            "enrolledAt": "2026-08-02T10:30:00Z"
        }
    ],
    "page": 1,
    "size": 10,
    "total": 8
}
```

---

# Cancel Enrollment *(Optional)*

Allows a user to unenroll from a course.

## Endpoint

```http
DELETE /enrollments/{enrollmentId}
```

## Behavior

- Removes the enrollment record.
- Deletes all associated lesson progress.
- User loses access to the course.

> If your business does **not** allow unenrollment, you can omit this API.

## Success Response

**HTTP Status**

```
200 OK
```

```json
{
    "message": "Enrollment cancelled successfully",
    "data": {}
}
```

---

# Error Codes

| Code           | Description           |
|----------------|-----------------------|
| ENROLLMENT_001 | Invalid request       |
| ENROLLMENT_002 | User already enrolled |
| ENROLLMENT_003 | Course is inactive    |
| ENROLLMENT_004 | Enrollment not found  |
| USER_001       | User not found        |
| COURSE_001     | Course not found      |
| AUTH_001       | Unauthorized          |

---

# Design Decisions

- A user can enroll in a course only once.
- `(user_id, course_id)` is enforced as a unique constraint.
- Every new enrollment starts with `courseCompletionStatus = INCOMPLETE`.
- Course status is automatically updated to `COMPLETE` when all lessons are completed.
- List API supports filtering by course status.
- Pagination, filtering, and sorting are supported.
- Responses follow the standard API response format.
- Users can view only their own enrollments.
- Administrators can view all enrollments.
- Instructors can view enrollments for their own courses.