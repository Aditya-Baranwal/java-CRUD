# Modules API

This document describes the current API contract for module endpoints.

---

# Resource

```text
/api/v1/modules
```

---

# Authorization

| Operation     | ADMIN |   INSTRUCTOR    | USER |
|---------------|:-----:|:---------------:|:----:|
| Create Module |  ✅   | ✅ (Own Course) |  ❌  |
| Update Module |  ✅   | ✅ (Own Course) |  ❌  |
| Get Module    |  ✅   |       ✅        |  ✅  |
| List Modules  |  ✅   |       ✅        |  ✅  |
| Delete Module |  ✅   | ✅ (Own Course) |  ❌  |

---

# Shared module response shapes

## Plain module shape

Used by:
- create module
- update module
- get module when `progress=false` or omitted
- list modules when `progress=false` or omitted

```json
{
  "moduleId": 10,
  "courseId": 1,
  "moduleTitle": "Spring Core",
  "moduleDescription": "Introduction to Spring Framework",
  "sequence": 1,
  "totalLessonCount": 3,
  "lessons": [],
  "isActive": true,
  "createdAt": "2026-08-02T10:30:00Z"
}
```

## Module + progress shape

Used only when `progress=true`.

```json
{
  "moduleId": 10,
  "courseId": 1,
  "moduleTitle": "Spring Core",
  "moduleDescription": "Introduction to Spring Framework",
  "sequence": 1,
  "lessons": [],
  "userId": 25,
  "isModuleCompleted": false,
  "completedLessonCount": 2,
  "totalLessonCount": 3,
  "isActive": true,
  "createdAt": "2026-08-02T10:30:00Z"
}
```

### Progress field semantics

- `userId`: echoes the queried user id.
- `totalLessonCount`: total number of **active** lessons in the module.
- `completedLessonCount`: number of **active** lessons completed by `userId`.
- `isModuleCompleted`: `true` only when the user has completed **all active lessons** in the module.
- If the user is not enrolled in the course, the API returns:
  - `isModuleCompleted: null`
  - `completedLessonCount: null`
  - `totalLessonCount`: still populated
- If the user is enrolled and the module has zero active lessons, the API returns:
  - `isModuleCompleted: false`
  - `completedLessonCount: 0`
  - `totalLessonCount: 0`
- When `includeLessons=true`, nested lessons always use the plain lesson shape, even when `progress=true`.

---

# Create Module

Creates a new module under an existing course.

## Endpoint

```http
POST /modules
```

## Request Headers

```http
Authorization: ******
Content-Type: application/json
```

## Request Body

```json
{
  "courseId": 1,
  "moduleTitle": "Spring Core",
  "moduleDescription": "Introduction to Spring Framework",
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
  "message": "Module created successfully",
  "data": {
    "moduleId": 10,
    "courseId": 1,
    "moduleTitle": "Spring Core",
    "moduleDescription": "Introduction to Spring Framework",
    "sequence": 1,
    "totalLessonCount": 0,
    "lessons": [],
    "isActive": true,
    "createdAt": "2026-08-02T10:30:00Z"
  },
  "timestamp": "2026-08-02T10:30:00Z"
}
```

## Possible Errors

| Status | Error Code  | Description |
|--------|-------------|-------------|
| 400 | MODULE_001 | Module payload is required |
| 400 | MODULE_002 | courseId is mandatory |
| 400 | MODULE_003 | moduleTitle is mandatory |
| 400 | MODULE_004 | sequence is mandatory and must be >= 1 |
| 403 | MODULE_011 | Instructor does not own the course |
| 404 | MODULE_010 | Course not found for module creation |
| 409 | MODULE_009 | Active module with the same sequence already exists within the course |
| 409 | MODULE_012 | Module cannot be created while course is in a non-mutable state |

---

# Update Module

Updates an existing module.

## Endpoint

```http
PUT /modules/{moduleId}
```

## Path Parameters

| Name | Type | Description |
|------|------|-------------|
| moduleId | Long | Module identifier |

## Request Body

```json
{
  "moduleTitle": "Spring Boot Fundamentals",
  "moduleDescription": "Updated Description",
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
  "message": "Module updated successfully",
  "data": {
    "moduleId": 10,
    "courseId": 1,
    "moduleTitle": "Spring Boot Fundamentals",
    "moduleDescription": "Updated Description",
    "sequence": 2,
    "totalLessonCount": 0,
    "lessons": [],
    "isActive": true,
    "createdAt": "2026-08-02T10:30:00Z"
  },
  "timestamp": "2026-08-02T11:00:00Z"
}
```

## Possible Errors

| Status | Error Code  | Description |
|--------|-------------|-------------|
| 400 | MODULE_001 | Module payload is required |
| 400 | MODULE_006 | Module course cannot be changed |
| 400 | MODULE_007 | moduleTitle cannot be blank |
| 400 | MODULE_008 | sequence must be >= 1 |
| 403 | MODULE_011 | Instructor does not own the course |
| 404 | MODULE_404 | Module not found |
| 409 | MODULE_009 | Active module with the same sequence already exists within the course |
| 409 | MODULE_013 | Module cannot be edited while course is in a non-mutable state |

---

# Get Module

Returns a module by identifier.

## Endpoint

```http
GET /modules/{moduleId}
```

## Path Parameters

| Name | Type | Description |
|------|------|-------------|
| moduleId | Long | Module identifier |

## Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| includeLessons | Boolean | No | false | Include lessons belonging to the module |
| userId | Long | No* | - | Required when `progress=true`; selects which user's module completion details to include |
| progress | Boolean | No | false | When `true`, returns the module + progress shape |

\* `userId` is mandatory whenever `progress=true`.

## Example

Plain module:

```http
GET /modules/10
```

Module with progress:

```http
GET /modules/10?progress=true&userId=25
```

Module with progress and lessons:

```http
GET /modules/10?includeLessons=true&progress=true&userId=25
```

## Success Response

**HTTP Status**

```text
200 OK
```

Plain module:

```json
{
  "message": "Module fetched successfully",
  "data": {
    "moduleId": 10,
    "courseId": 1,
    "moduleTitle": "Spring Core",
    "moduleDescription": "Introduction to Spring Framework",
    "sequence": 1,
    "totalLessonCount": 3,
    "lessons": [],
    "isActive": true,
    "createdAt": "2026-08-02T10:30:00Z"
  },
  "timestamp": "2026-08-02T10:30:00Z"
}
```

Module with progress:

```json
{
  "message": "Module fetched successfully",
  "data": {
    "moduleId": 10,
    "courseId": 1,
    "moduleTitle": "Spring Core",
    "moduleDescription": "Introduction to Spring Framework",
    "sequence": 1,
    "lessons": [],
    "userId": 25,
    "isModuleCompleted": false,
    "completedLessonCount": 2,
    "totalLessonCount": 3,
    "isActive": true,
    "createdAt": "2026-08-02T10:30:00Z"
  },
  "timestamp": "2026-08-02T10:30:00Z"
}
```

## Possible Errors

| Status | Error Code  | Description |
|--------|-------------|-------------|
| 400 | MODULE_015 | userId is mandatory when progress=true |
| 404 | MODULE_404 | Module not found |

---

# List Modules

Returns a paginated list of modules.

## Endpoint

```http
GET /modules
```

## Query Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| courseId | Long | Yes | - | Course whose modules are to be fetched |
| userId | Long | No* | - | Required when `progress=true`; selects which user's module completion details to include |
| progress | Boolean | No | false | When `true`, returns each item in the module + progress shape |
| active | Boolean | No | true | Filter active/inactive modules |
| pageNo | Integer | Yes | 1 | Page number |
| pageSize | Integer | Yes | 10 | Page size |
| sortBy | String | No | sequence | Sort field |
| sortOrder | String | No | asc | asc / desc |

\* `userId` is mandatory whenever `progress=true`.

## Example

Plain module list:

```http
GET /modules?courseId=1&pageNo=1&pageSize=10&sortBy=sequence&sortOrder=asc
```

Module list with progress:

```http
GET /modules?courseId=1&progress=true&userId=25&pageNo=1&pageSize=10
```

## Success Response

**HTTP Status**

```text
200 OK
```

Plain module list:

```json
{
  "message": "Modules fetched successfully",
  "data": [
    {
      "moduleId": 10,
      "courseId": 1,
      "moduleTitle": "Spring Core",
      "moduleDescription": "Introduction to Spring Framework",
      "sequence": 1,
      "totalLessonCount": 3,
      "isActive": true,
      "createdAt": "2026-08-02T10:30:00Z"
    }
  ],
  "page": 1,
  "size": 10,
  "total": 1,
  "timestamp": "2026-08-02T10:30:00Z"
}
```

Module list with progress:

```json
{
  "message": "Modules fetched successfully",
  "data": [
    {
      "moduleId": 10,
      "courseId": 1,
      "moduleTitle": "Spring Core",
      "moduleDescription": "Introduction to Spring Framework",
      "sequence": 1,
      "userId": 25,
      "isModuleCompleted": false,
      "completedLessonCount": 2,
      "totalLessonCount": 3,
      "isActive": true,
      "createdAt": "2026-08-02T10:30:00Z"
    }
  ],
  "page": 1,
  "size": 10,
  "total": 1,
  "timestamp": "2026-08-02T10:30:00Z"
}
```

## Possible Errors

| Status | Error Code  | Description |
|--------|-------------|-------------|
| 400 | MODULE_002 | courseId is mandatory |
| 400 | MODULE_015 | userId is mandatory when progress=true |
| 404 | MODULE_010 | Course not found for module creation |

---

# Delete Module

Performs a soft delete by marking the module as inactive.

## Endpoint

```http
DELETE /modules/{moduleId}
```

## Path Parameters

| Name | Type | Description |
|------|------|-------------|
| moduleId | Long | Module identifier |

## Behavior

- Module is **not physically deleted**.
- Updates `is_active = false`.
- Existing lessons remain unchanged.
- Existing user progress is retained.

## Success Response

**HTTP Status**

```text
200 OK
```

```json
{
  "message": "Module inactivated successfully",
  "data": {},
  "timestamp": "2026-08-02T11:30:00Z"
}
```

## Possible Errors

| Status | Error Code  | Description |
|--------|-------------|-------------|
| 403 | MODULE_011 | Instructor does not own the course |
| 404 | MODULE_404 | Module not found |
| 409 | MODULE_014 | Module cannot be deleted while course is in a non-mutable state |

---

# Design Decisions

- Modules belong to exactly one course.
- Module sequence is unique among active modules within a course.
- Sequence determines the display order.
- Soft delete is implemented using the `is_active` flag.
- Plain read responses always include `totalLessonCount`.
- Progress read responses are enabled with `progress=true` and require `userId`.
- Module completion is derived from active lesson completion only.
- Nested lessons stay in the plain lesson shape even in module progress mode.
