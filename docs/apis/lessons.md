# Lessons API

This document describes the current API contract for lesson endpoints.

---

# Resource

```text
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
| Delete Lesson |  ✅   | ✅ (Own Course) |  ❌  |

---

# Shared lesson response shapes

## Plain lesson shape

Used by:
- create lesson
- update lesson
- get lesson when `progress=false` or omitted
- list lessons when `progress=false` or omitted

```json
{
  "lessonId": 100,
  "moduleId": 10,
  "contentType": "MP4",
  "contentLink": "https://cdn.example.com/videos/introduction.mp4",
  "sequence": 1,
  "isActive": true,
  "createdAt": "2026-08-02T10:30:00Z"
}
```

## Lesson + progress shape

Used only when `progress=true`.

```json
{
  "lessonId": 100,
  "moduleId": 10,
  "contentType": "MP4",
  "contentLink": "https://cdn.example.com/videos/introduction.mp4",
  "sequence": 1,
  "userId": 25,
  "progressId": 9001,
  "lessonStatus": "STARTED",
  "isLessonCompleted": false,
  "progressStartedAt": "2026-08-02T10:35:00Z",
  "progressCompletedAt": null,
  "isActive": true,
  "createdAt": "2026-08-02T10:30:00Z"
}
```

### Progress field semantics

- `userId`: echoes the queried user id.
- `progressId`: progress row id for that lesson/user when a row exists.
- `lessonStatus`: `UNSTARTED`, `STARTED`, or `FINISHED`.
- `isLessonCompleted`: `true` only when `lessonStatus=FINISHED`.
- `progressStartedAt` / `progressCompletedAt`: copied from the progress record when present.
- If the user is enrolled but no progress row exists, the API returns:
  - `progressId: null`
  - `lessonStatus: "UNSTARTED"`
  - `isLessonCompleted: false`
  - `progressStartedAt: null`
  - `progressCompletedAt: null`
- If the user is not enrolled, the API returns:
  - `progressId: null`
  - `lessonStatus: null`
  - `isLessonCompleted: null`
  - `progressStartedAt: null`
  - `progressCompletedAt: null`

---

# Create Lesson

Creates a new lesson under an existing module.

## Endpoint

```http
POST /lessons
```

## Request Headers

```http
Authorization: Bearer <token>
Content-Type: application/json
```

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

```text
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
  },
  "timestamp": "2026-08-02T10:30:00Z"
}
```

## Possible Errors

| Status | Error Code  | Description |
|--------|-------------|-------------|
| 400 | LESSON_001 | Lesson payload is required |
| 400 | LESSON_002 | moduleId is mandatory |
| 400 | LESSON_003 | contentType is mandatory |
| 400 | LESSON_004 | contentLink is mandatory |
| 400 | LESSON_005 | sequence is mandatory and must be >= 1 |
| 403 | LESSON_011 | Instructor does not own the course |
| 404 | MODULE_404 | Module not found |
| 409 | LESSON_010 | Active lesson with the same sequence already exists within the module |
| 409 | LESSON_012 | Lesson cannot be created while course is in a non-mutable state |

---

# Update Lesson

Updates an existing lesson.

## Endpoint

```http
PUT /lessons/{lessonId}
```

## Path Parameters

| Name | Type | Description |
|------|------|-------------|
| lessonId | Long | Lesson identifier |

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

```text
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
  },
  "timestamp": "2026-08-02T11:00:00Z"
}
```

## Possible Errors

| Status | Error Code  | Description |
|--------|-------------|-------------|
| 400 | LESSON_001 | Lesson payload is required |
| 400 | LESSON_007 | Lesson module cannot be changed |
| 400 | LESSON_008 | contentLink cannot be blank |
| 400 | LESSON_009 | sequence must be >= 1 |
| 403 | LESSON_011 | Instructor does not own the course |
| 404 | LESSON_404 | Lesson not found |
| 409 | LESSON_010 | Active lesson with the same sequence already exists within the module |
| 409 | LESSON_013 | Lesson cannot be edited while course is in a non-mutable state |

---

# Get Lesson

Returns a lesson by identifier.

## Endpoint

```http
GET /lessons/{lessonId}
```

## Path Parameters

| Name | Type | Description |
|------|------|-------------|
| lessonId | Long | Lesson identifier |

## Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| userId | Long | No* | - | Required when `progress=true`; selects which user's progress to include |
| progress | Boolean | No | false | When `true`, returns the lesson + progress shape |

\* `userId` is mandatory whenever `progress=true`.

## Example

Plain lesson:

```http
GET /lessons/100
```

Lesson with progress:

```http
GET /lessons/100?progress=true&userId=25
```

## Success Response

### Plain lesson response

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
  },
  "timestamp": "2026-08-02T10:40:00Z"
}
```

### Progress-enriched response

```json
{
  "message": "Lesson fetched successfully",
  "data": {
    "lessonId": 100,
    "moduleId": 10,
    "contentType": "MP4",
    "contentLink": "https://cdn.example.com/videos/introduction.mp4",
    "sequence": 1,
    "userId": 25,
    "progressId": 9001,
    "lessonStatus": "STARTED",
    "isLessonCompleted": false,
    "progressStartedAt": "2026-08-02T10:35:00Z",
    "progressCompletedAt": null,
    "isActive": true,
    "createdAt": "2026-08-02T10:30:00Z"
  },
  "timestamp": "2026-08-02T10:40:00Z"
}
```

## Possible Errors

| Status | Error Code  | Description |
|--------|-------------|-------------|
| 400 | LESSON_015 | userId is mandatory when progress=true |
| 404 | LESSON_404 | Lesson not found |

---

# List Lessons

Returns a paginated list of lessons.

## Endpoint

```http
GET /lessons
```

## Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| moduleId | Long | Yes | - | Module whose lessons should be fetched |
| userId | Long | No* | - | Required when `progress=true`; selects which user's progress to include |
| progress | Boolean | No | false | When `true`, returns lesson + progress list items |
| active | Boolean | No | true | Filters by active flag when applicable |
| pageNo | Integer | Yes | 1 | 1-based page number |
| pageSize | Integer | Yes | 10 | Page size |
| sortBy | String | No | sequence | Sort field |
| sortOrder | String | No | asc | `asc` or `desc` |

\* `userId` is mandatory whenever `progress=true`.

## Examples

Plain lesson list:

```http
GET /lessons?moduleId=10&pageNo=1&pageSize=10
```

Progress-enriched lesson list:

```http
GET /lessons?moduleId=10&progress=true&userId=25&pageNo=1&pageSize=10
```

## Success Response

### Plain lesson list

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
  "total": 20,
  "timestamp": "2026-08-02T10:45:00Z"
}
```

### Progress-enriched lesson list

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
      "userId": 25,
      "progressId": 9001,
      "lessonStatus": "STARTED",
      "isLessonCompleted": false,
      "progressStartedAt": "2026-08-02T10:35:00Z",
      "progressCompletedAt": null,
      "isActive": true,
      "createdAt": "2026-08-02T10:30:00Z"
    }
  ],
  "page": 1,
  "size": 10,
  "total": 20,
  "timestamp": "2026-08-02T10:45:00Z"
}
```

## Possible Errors

| Status | Error Code  | Description |
|--------|-------------|-------------|
| 400 | LESSON_002 | moduleId is mandatory |
| 400 | LESSON_015 | userId is mandatory when progress=true |
| 401 | AUTH_001 | Unauthorized |
| 404 | MODULE_404 | Module not found |

---

# Delete Lesson

Performs a soft delete by marking the lesson inactive.

## Endpoint

```http
DELETE /lessons/{lessonId}
```

## Path Parameters

| Name | Type | Description |
|------|------|-------------|
| lessonId | Long | Lesson identifier |

## Behavior

- The lesson is not physically deleted.
- The API sets `isActive=false`.
- Existing progress rows are retained.

## Success Response

**HTTP Status**

```text
200 OK
```

```json
{
  "message": "Lesson deleted successfully",
  "data": {},
  "timestamp": "2026-08-02T11:10:00Z"
}
```

## Possible Errors

| Status | Error Code  | Description |
|--------|-------------|-------------|
| 403 | LESSON_011 | Instructor does not own the course |
| 404 | LESSON_404 | Lesson not found |
| 409 | LESSON_014 | Lesson cannot be deleted while course is in a non-mutable state |

---

# Design notes

- `progress=false` or omitted keeps lesson responses in the plain lesson shape.
- `progress=true` switches `GET /lessons` and `GET /lessons/{lessonId}` to progress-enriched response shapes.
- `userId` is only used for progress-enriched reads.
- `progressId` is returned only in progress-enriched responses and is nullable when no progress row exists.
- Progress details are visibility-gated by enrollment:
  - enrolled + progress row -> actual progress values
  - enrolled + no progress row -> `UNSTARTED`, `isLessonCompleted=false`
  - not enrolled -> progress fields are `null`
