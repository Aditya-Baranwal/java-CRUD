# Progress API

This document describes the current API contract for progress endpoints.

---

# Resource

```text
/api/v1/progress
```

---

# Update Progress

Updates progress by identifier.

## Endpoint

```http
PUT /progress/{progressId}
```

## Path Parameters

| Name | Type | Description |
|------|------|-------------|
| progressId | Long | Progress identifier |

## Request Body

```json
{
  "lessonStatus": "STARTED"
}
```

## Success Response

**HTTP Status**

```text
200 OK
```

```json
{
  "message": "Progress updated successfully",
  "data": {
    "progressId": 1001,
    "lessonId": 10,
    "lessonTitle": null,
    "userId": 25,
    "moduleId": 5,
    "courseId": 1,
    "lessonStatus": "STARTED",
    "startedAt": "2026-08-02T10:30:00Z",
    "completedAt": null
  },
  "timestamp": "2026-08-02T10:30:00Z"
}
```

## Possible Errors

| Status | Description |
|--------|-------------|
| 400 | Bad request |
| 403 | Forbidden |
| 404 | Not found |
| 409 | Conflict |
