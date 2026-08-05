# Modules API

This document defines all Module-related APIs exposed by the Learning Management System (LMS).

---

# Resource

```
/api/v1/modules
```

---

# Authorization

| Operation | ADMIN | INSTRUCTOR | USER |
|-----------|:-----:|:----------:|:----:|
| Create Module | ✅ | ✅ (Own Course) | ❌ |
| Update Module | ✅ | ✅ (Own Course) | ❌ |
| Get Module | ✅ | ✅ | ✅ |
| List Modules | ✅ | ✅ | ✅ |
| Delete Module | ✅ | ❌ | ❌ |

---

# Create Module

Creates a new module under an existing course.

## Endpoint

```http
POST /modules
```

## Request Headers

```http
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

## Validation

| Field | Validation |
|--------|------------|
| courseId | Required, Course must exist |
| moduleTitle | Required, Maximum 50 characters |
| moduleDescription | Optional, Maximum 100 characters |
| sequence | Required, Positive Integer |

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

```
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
        "isActive": true,
        "createdAt": "2026-08-02T10:30:00Z"
    }
}
```

## Possible Errors

| Status | Error Code | Description |
|---------|------------|-------------|
| 400 | MODULE_001 | Invalid request payload |
| 400 | MODULE_002 | Module title is mandatory |
| 404 | COURSE_001 | Course not found |
| 409 | MODULE_003 | Sequence already exists |
| 403 | AUTH_001 | Unauthorized |

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
| moduleId | Long | Module Identifier |

## Validation

| Field | Validation |
|--------|------------|
| moduleTitle | Optional, Maximum 50 characters |
| moduleDescription | Optional, Maximum 100 characters |
| sequence | Optional, Positive Integer |
| isActive | Optional |

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

```
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
        "isActive": true,
        "createdAt": "2026-08-02T10:30:00Z"
    }
}
```

## Possible Errors

| Status | Error Code | Description |
|---------|------------|-------------|
| 400 | MODULE_001 | Invalid request |
| 404 | MODULE_004 | Module not found |
| 404 | COURSE_001 | Course not found |
| 409 | MODULE_003 | Sequence already exists |
| 403 | AUTH_001 | Unauthorized |

---

# Get Module

Returns a module by its identifier.

## Endpoint

```http
GET /modules/{moduleId}
```

## Path Parameters

| Name | Type | Description |
|------|------|-------------|
| moduleId | Long | Module Identifier |

## Query Parameters

| Parameter | Type | Required | Default | Description |
|------------|------|----------|---------|-------------|
| includeLessons | Boolean | No | false | Include lessons belonging to the module |

## Success Response

**HTTP Status**

```
200 OK
```

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
        "isActive": true,
        "createdAt": "2026-08-02T10:30:00Z"
    }
}
```

## Possible Errors

| Status | Error Code | Description |
|---------|------------|-------------|
| 404 | MODULE_004 | Module not found |

---

# List Modules

Returns a paginated list of modules.

## Endpoint

```http
GET /modules
```

## Query Parameters

| Parameter | Type | Required | Default | Description |
|------------|------|----------|---------|-------------|
| courseId | Long | Yes | - | Course whose modules are to be fetched |
| active | Boolean | No | true | Filter active/inactive modules |
| pageNo | Integer | Yes | 1 | Page number |
| pageSize | Integer | Yes | 10 | Page size |
| sortBy | String | No | sequence | Sort field |
| sortOrder | String | No | asc | asc / desc |

## Example

```http
GET /modules?courseId=1&pageNo=1&pageSize=10&sortBy=sequence&sortOrder=asc
```

## Success Response

**HTTP Status**

```
200 OK
```

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
            "isActive": true,
            "createdAt": "2026-08-02T10:30:00Z"
        }
    ],
    "page": 1,
    "size": 10,
    "total": 8
}
```

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
| moduleId | Long | Module Identifier |

## Behavior

- Module is **not physically deleted**.
- Updates `is_active = false`.
- Existing lessons remain unchanged.
- Existing user progress is retained.

## Success Response

**HTTP Status**

```
200 OK
```

```json
{
    "message": "Module inactivated successfully",
    "data": {}
}
```

## Possible Errors

| Status | Error Code | Description |
|---------|------------|-------------|
| 404 | MODULE_004 | Module not found |
| 409 | MODULE_005 | Module cannot be deleted |
| 403 | AUTH_001 | Unauthorized |

---

# Error Codes

| Code | Description |
|------|-------------|
| MODULE_001 | Invalid request |
| MODULE_002 | Module title is mandatory |
| MODULE_003 | Module sequence already exists within the course |
| MODULE_004 | Module not found |
| MODULE_005 | Module cannot be deleted |
| COURSE_001 | Course not found |
| AUTH_001 | Unauthorized |

---

# Design Decisions

- Modules belong to exactly one course.
- Module sequence is unique within a course.
- Sequence determines the display order.
- Soft delete is implemented using the `is_active` flag.
- Pagination, sorting, and filtering are supported.
- Responses follow the standard API response format.
- Only course owners (Instructor) or ADMIN can modify modules.
- USER role has read-only access.
```