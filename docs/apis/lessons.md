# Lessons API

This document defines all Lesson-related APIs exposed by the Learning Management System (LMS).

---

# Resource

```
/api/v1/lessons
```

---

# Authorization

| Operation     | ADMIN |   INSTRUCTOR    | USER |
|---------------|:-----:|:---------------:|:----:|
| Create Lesson |  ✅   | ✅ (Own Course) |  ❌  |
| Update Lesson |  ✅   | ✅ (Own Course) |  ❌  |
| Get Lesson    |  ✅   |       ✅        |  ✅  |
| List Lessons  |  ✅   |       ✅        |  ✅  |
| Delete Lesson |  ✅   |       ❌        |  ❌  |

---

# Create Lesson

Creates a new lesson under an existing module.

## Endpoint

```http
POST /lessons
```

## Request Headers

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

## Validation

| Field       | Validation                                   |
|-------------|----------------------------------------------|
| moduleId    | Required, Module must exist                  |
| contentType | Required, Must be one of MP3, MP4, PDF, TEXT |
| contentLink | Required, Valid URL                          |
| sequence    | Required, Positive Integer                   |

## Request Body

```json
{
    "moduleId": 10,
    "contentType": "MP4",
    "contentLink": "https://cdn.example.com/videos/introduction.mp4",
    "sequence": 1
}
```

## Success Response

**HTTP Status**

```
201 Created
```

```json
{
    "message": "Lesson created successfully",
    "data": {
        "lessonId": 100,
        "moduleId": 10,
        "contentType": "MP4",
        "contentLink": "https://cdn.example.com/videos/introduction.mp4",
        "sequence": 1,
        "isActive": true,
        "createdAt": "2026-08-02T10:30:00Z"
    }
}
```

## Possible Errors

| Status | Error Code | Description                    |
|--------|------------|--------------------------------|
| 400    | LESSON_001 | Invalid request payload        |
| 400    | LESSON_002 | Invalid content type           |
| 400    | LESSON_003 | Invalid content URL            |
| 404    | MODULE_001 | Module not found               |
| 409    | LESSON_004 | Lesson sequence already exists |
| 403    | AUTH_001   | Unauthorized                   |

---

# Update Lesson

Updates an existing lesson.

## Endpoint

```http
PUT /lessons/{lessonId}
```

## Path Parameters

| Name     | Type | Description       |
|----------|------|-------------------|
| lessonId | Long | Lesson Identifier |

## Validation

| Field       | Validation                 |
|-------------|----------------------------|
| contentType | Optional, Valid Enum       |
| contentLink | Optional, Valid URL        |
| sequence    | Optional, Positive Integer |
| isActive    | Optional                   |

## Request Body

```json
{
    "contentType": "PDF",
    "contentLink": "https://cdn.example.com/docs/introduction.pdf",
    "sequence": 2,
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
    "message": "Lesson updated successfully",
    "data": {
        "lessonId": 100,
        "moduleId": 10,
        "contentType": "PDF",
        "contentLink": "https://cdn.example.com/docs/introduction.pdf",
        "sequence": 2,
        "isActive": true,
        "createdAt": "2026-08-02T10:30:00Z"
    }
}
```

## Possible Errors

| Status | Error Code | Description                    |
|--------|------------|--------------------------------|
| 400    | LESSON_001 | Invalid request                |
| 404    | LESSON_005 | Lesson not found               |
| 404    | MODULE_001 | Module not found               |
| 409    | LESSON_004 | Lesson sequence already exists |
| 403    | AUTH_001   | Unauthorized                   |

---

# Get Lesson

Returns a lesson by its identifier.

## Endpoint

```http
GET /lessons/{lessonId}
```

## Path Parameters

| Name     | Type | Description       |
|----------|------|-------------------|
| lessonId | Long | Lesson Identifier |

## Success Response

**HTTP Status**

```
200 OK
```

```json
{
    "message": "Lesson fetched successfully",
    "data": {
        "lessonId": 100,
        "moduleId": 10,
        "contentType": "MP4",
        "contentLink": "https://cdn.example.com/videos/introduction.mp4",
        "sequence": 1,
        "isActive": true,
        "createdAt": "2026-08-02T10:30:00Z"
    }
}
```

## Possible Errors

| Status | Error Code | Description      |
|--------|------------|------------------|
| 404    | LESSON_005 | Lesson not found |

---

# List Lessons

Returns a paginated list of lessons.

## Endpoint

```http
GET /lessons
```

## Query Parameters

| Parameter | Type    | Required | Default  | Description                            |
|-----------|---------|----------|----------|----------------------------------------|
| moduleId  | Long    | Yes      | -        | Module whose lessons are to be fetched |
| userId    | Long    | No       | -        | Include lesson progress for a user     |
| active    | Boolean | No       | true     | Filter active/inactive lessons         |
| pageNo    | Integer | Yes      | 1        | Page number                            |
| pageSize  | Integer | Yes      | 10       | Page size                              |
| sortBy    | String  | No       | sequence | Sort field                             |
| sortOrder | String  | No       | asc      | asc / desc                             |

## Example

```http
GET /lessons?moduleId=10&pageNo=1&pageSize=10
```

or

```http
GET /lessons?moduleId=10&userId=25&pageNo=1&pageSize=10
```

## Success Response

### Without User Progress

**HTTP Status**

```
200 OK
```

```json
{
    "message": "Lessons fetched successfully",
    "data": [
        {
            "lessonId": 100,
            "moduleId": 10,
            "contentType": "MP4",
            "contentLink": "https://cdn.example.com/videos/introduction.mp4",
            "sequence": 1,
            "isActive": true,
            "createdAt": "2026-08-02T10:30:00Z"
        }
    ],
    "page": 1,
    "size": 10,
    "total": 20
}
```

### With User Progress

```json
{
    "message": "Lessons with progress fetched successfully",
    "data": [
        {
            "lessonId": 100,
            "moduleId": 10,
            "userId": 25,
            "lessonStatus": "STARTED",
            "contentType": "MP4",
            "contentLink": "https://cdn.example.com/videos/introduction.mp4",
            "sequence": 1,
            "isActive": true,
            "createdAt": "2026-08-02T10:30:00Z"
        }
    ],
    "page": 1,
    "size": 10,
    "total": 20
}
```

---

# Delete Lesson

Performs a soft delete by marking the lesson as inactive.

## Endpoint

```http
DELETE /lessons/{lessonId}
```

## Path Parameters

| Name     | Type | Description       |
|----------|------|-------------------|
| lessonId | Long | Lesson Identifier |

## Behavior

- Lesson is **not physically deleted**.
- Updates `is_active = false`.
- Existing user progress is retained.
- Enrolled users can no longer access inactive lessons.

## Success Response

**HTTP Status**

```
200 OK
```

```json
{
    "message": "Lesson inactivated successfully",
    "data": {}
}
```

## Possible Errors

| Status | Error Code | Description              |
|--------|------------|--------------------------|
| 404    | LESSON_005 | Lesson not found         |
| 409    | LESSON_006 | Lesson cannot be deleted |
| 403    | AUTH_001   | Unauthorized             |

---

# Error Codes

| Code       | Description                                      |
|------------|--------------------------------------------------|
| LESSON_001 | Invalid request                                  |
| LESSON_002 | Invalid content type                             |
| LESSON_003 | Invalid content URL                              |
| LESSON_004 | Lesson sequence already exists within the module |
| LESSON_005 | Lesson not found                                 |
| LESSON_006 | Lesson cannot be deleted                         |
| MODULE_001 | Module not found                                 |
| AUTH_001   | Unauthorized                                     |

---

# Design Decisions

- Every lesson belongs to exactly one module.
- Lesson sequence determines the learning order within a module.
- Lesson sequence must be unique within a module.
- Lessons are soft deleted using the `is_active` flag.
- User progress is never deleted when a lesson is inactivated.
- List API optionally enriches lesson data with user progress when `userId` is provided.
- Pagination, filtering, and sorting are supported for all list operations.
- Responses follow the standard API response format.
- Only course owners (Instructor) or ADMIN can create, update, or delete lessons.
- USER role has read-only access to lesson metadata.