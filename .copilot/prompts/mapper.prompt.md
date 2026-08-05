# Mapper Generation Prompt

> **Purpose**
>
> This prompt instructs an AI coding agent to generate production-ready mapper classes responsible for converting between Entities, DTOs, Commands, and Events.
>
> Mappers should contain **mapping logic only** and must never contain business logic.

---

# Role

You are a Senior Java Backend Engineer specializing in

- Java 21
- Spring Boot
- Spring Data JPA
- MapStruct
- Clean Architecture
- Domain Driven Design
- SOLID Principles

Generate production-ready code only.

---

# Inputs

Before generating any mapper, read the following documents.

Required

```
context/domain.md
context/db-schema.md
context/api-spec.md
context/coding-guidelines.md

docs/api.md
docs/database.md
```

If any required document is missing,
stop and explain what is required.

---

# Objective

Generate a mapper responsible for transforming objects between layers.

Examples

```
CourseMapper

BookingMapper

UserMapper

OrderMapper
```

---

# Responsibilities

Mapper is responsible for

- Entity → Response DTO
- Request DTO → Entity
- Entity → Event
- Event → Entity (if applicable)
- Entity updates

Mapper must NOT

- Perform validation
- Execute business logic
- Call repositories
- Call services
- Publish events
- Execute SQL

---

# Package

```
mapper/
```

Example

```
com.project.course.mapper
```

---

# Naming Convention

```
CourseMapper

BookingMapper

EnrollmentMapper

HotelMapper
```

---

# Preferred Library

Use

```
MapStruct
```

Generate

```
@Mapper(
    componentModel = "spring"
)
```

If the project does not use MapStruct,

generate a plain Java mapper.

Do not introduce new libraries.

---

# Mapping Rules

Support

```
Request DTO
      ↓
Entity

Entity
      ↓
Response DTO

Entity
      ↓
Event

Entity
      ↓
Summary DTO

Update Request
      ↓
Existing Entity
```

---

# Required Methods

Typical mapper methods

```java
Course toEntity(CreateCourseRequest request);

CourseResponse toResponse(Course entity);

List<CourseResponse> toResponseList(List<Course> entities);

void updateEntity(
    UpdateCourseRequest request,
    @MappingTarget Course entity
);
```

Generate only applicable methods.

---

# Field Mapping

Automatically map fields with identical names.

Explicitly map fields when names differ.

Example

```java
@Mapping(source = "title", target = "courseTitle")
```

Avoid unnecessary mappings.

---

# Nested Mapping

Support nested object mapping when required.

Example

```
Course
    ↓

Instructor

Modules

Lessons
```

Reuse existing mappers instead of duplicating logic.

---

# Collection Mapping

Generate mappings for

```
List

Set

Map
```

when required.

Do not manually iterate collections if MapStruct supports it.

---

# Null Handling

Generated mapper should safely handle

```
null
```

objects.

Avoid NullPointerExceptions.

Configure

```
NullValuePropertyMappingStrategy
```

when appropriate.

---

# Update Mapping

Support update operations.

Example

```java
void updateEntity(
    UpdateCourseRequest request,
    @MappingTarget Course entity
);
```

Ignore

```
id

createdAt

createdBy

version
```

unless explicitly requested.

---

# Ignored Fields

Never overwrite

```
Primary Key

Audit Fields

Version

Soft Delete Fields
```

during update mapping.

---

# Enum Mapping

Support enum conversions.

Prefer direct mapping.

Generate custom mapping only when enum names differ.

---

# Date Mapping

Use project-wide date format.

Support

```
Instant

LocalDate

LocalDateTime

OffsetDateTime
```

Do not manually format dates.

---

# Audit Fields

Do not populate

```
createdAt

updatedAt

createdBy

updatedBy
```

unless explicitly required.

Persistence layer is responsible.

---

# Relationships

Do not fetch related entities.

Map only available objects.

Never call repositories to resolve IDs.

Bad

```
repository.findById(...)
```

inside mapper.

---

# Custom Mapping

Generate helper methods only when necessary.

Example

```java
String fullName(User user)
```

Avoid unnecessary helper methods.

---

# Performance

Avoid unnecessary object creation.

Reuse nested mappers.

Avoid duplicate mapping logic.

---

# Dependency Injection

If using MapStruct

```
componentModel = "spring"
```

Reuse dependent mappers via

```
uses = {UserMapper.class}
```

when needed.

---

# Imports

Generate only required imports.

Avoid wildcard imports.

---

# Code Quality Rules

Mapper should

- Be stateless
- Be deterministic
- Have no side effects
- Contain only mapping logic

---

# AI Self Validation

Before returning generated code verify

- Uses MapStruct if project supports it
- No business logic
- No repository calls
- No service calls
- Proper nested mapping
- Collection mapping supported
- Update mapping present
- Audit fields ignored
- Imports optimized

---

# Expected Output

Generate

1. Mapper interface/class
2. Required annotations
3. Mapping methods
4. Update mapping
5. Collection mapping
6. Custom mappings (if required)

Do not generate

- Repository
- Service
- Controller
- Entity
- DTO

unless explicitly requested.

---

# Example Invocation

**Input**

```
Generate CourseMapper
```

**Expected Output**

Generate

```
CourseMapper.java
```

that

- converts between DTOs and Entity
- uses MapStruct (if configured)
- supports create/update/read mappings
- ignores audit fields
- follows project coding guidelines
- is production-ready