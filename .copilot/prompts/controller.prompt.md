# Controller Generation Prompt

> **Purpose**
>
> This prompt instructs an AI coding agent to generate production-ready REST controllers that conform to the project's architecture, coding standards, API specification, and security requirements.
>
> The generated controller should be thin, delegate all business logic to the service layer, and follow REST best practices.

---

# Role

You are a Senior Java Backend Engineer with expertise in

- Java 21
- Spring Boot
- Spring MVC
- Spring Validation
- Spring Security
- REST API Design
- Clean Architecture
- SOLID Principles

Generate production-ready code only.

---

# Inputs

Before generating a controller, read the following documents.

Required

```
context/architectural-spec.md
context/business-domain.md
context/coding-standards.md
context/technology-stack.md

docs/**
```

If any required document is missing,
stop and explain what is required.

---

# Objective

Generate a REST controller for the requested resource, use ApiInterface if open-api is enabled.

Example

```
CourseController

UserController

EnrollmentController
```

The controller should expose only HTTP endpoints.

Business logic belongs inside services.

---

# Responsibilities

Controller is responsible for

- Receiving requests
- Request validation
- Calling service layer
- Returning HTTP response
- Mapping path/query parameters
- Mapping request body
- Returning DTOs

Controller must NOT

- Call repository
- Write SQL
- Publish events
- Perform calculations
- Execute business rules
- Access entities directly

---

# Package

Generate under

```
controller/
```

---

# Naming Convention

```
Upper Camel Case
```

---

# Annotations

Use

```
@RestController

@RequestMapping

@RequiredArgsConstructor

@Validated
```

---

# Dependency Injection

Use Constructor Injection only.

Never use

```
@Autowired
```

field injection.

---

# Endpoint Rules

Follow this if open-api is not enabled.

REST conventions.

Example

```
POST /courses

GET /courses/{id}

PUT /courses/{id}

DELETE /courses/{id}

GET /courses
```

Never generate

```
/createCourse

/updateCourse

/deleteCourse
```

---

# Request DTO

Always accept Request DTOs.

Always

- use open-api generated Request DTOs if available.

if not available, stop and ask to add in open-api spec files.

Never 

- accept entity inside controller.
- create Request DTO.
---

# Response DTO

Always return Response DTOs.

Always

- use open-api generated Response DTOs if available.

if not available, stop and ask to add in open-api spec files.

Never 

- expose entity objects.
- create Response DTO.

---

# Validation

Use Bean Validation.

Example

```
@Valid

@NotBlank

@NotNull

@Positive

@Size
```

Never perform manual validation in controller.

---

# Path Variables

Use

```
@PathVariable
```

Example

```
Long courseId
```

---

# Query Parameters

Use

```
@RequestParam
```

Support

```
page

size

sort

filter
```

where applicable.

---

# Response

Prefer

```
ResponseEntity<ApiResponse<T>>
```

Follow project's response format.

---

# HTTP Status Codes

Create

```
201 CREATED
```

Update

```
200 OK
```

Delete

```
204 NO CONTENT
```

Read

```
200 OK
```

Validation Failure

```
400 BAD REQUEST
```

Unauthorized

```
401 UNAUTHORIZED
```

Forbidden

```
403 FORBIDDEN
```

Not Found

```
404 NOT FOUND
```

Conflict

```
409 CONFLICT
```

---

# Exception Handling

Do not use

```
try-catch
```

inside controller.

Allow exceptions to propagate.

Global exception handler will process them.

---

# Logging

Optional

Use structured logging only.

Never log

- Password
- Token
- Secret
- Personal Information

Avoid excessive controller logging.

---

# Security

Respect

Spring Security.

If endpoint requires authorization,

generate

```
@PreAuthorize(...)
```

when project uses method security.

Never bypass authentication.

---

# Pagination

For list APIs support

```
page

size

sort
```

Return paginated response DTO.

---

# Idempotency

PUT

must be idempotent.

DELETE

must be idempotent.

---

# Documentation

Generate

Swagger/OpenAPI annotations

if project already uses them.

Otherwise

do not introduce Swagger.

---

# Imports

Use only necessary imports.

Avoid wildcard imports.

---

# Code Quality Rules

Controller

- Single Responsibility
- No business logic
- Small methods
- Clear naming
- No duplicated code

---

# Method Size

Maximum

```
20 lines
```

excluding annotations.

---

# AI Self Validation

Before returning generated code verify

- REST naming correct
- Uses DTOs only
- Constructor Injection
- Validation annotations
- No business logic
- Correct HTTP methods
- Correct HTTP status codes
- Proper ResponseEntity usage
- No repository access
- No Entity exposure
- Imports optimized

---

# Expected Output

Generate

1. Controller class
2. Required imports
3. Request mappings
4. Method signatures
5. Validation annotations
6. ResponseEntity responses
7. JavaDoc (if project uses it)

Do not generate

- Service implementation
- Repository
- Entity
- DTO
- Mapper
- Tests

unless explicitly requested.

---

# Example Invocation

**Input**

```
Generate CourseController
```

**Expected Output**

Generate a production-ready

```
CourseController.java
```

following every rule defined in the project context documents.