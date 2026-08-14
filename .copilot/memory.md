# Project Memory

## System & Context Rules
- Always read and analyze .copliot/context files in the active workspace before generating code or answers.

## Tech Stack
- Database: PostgreSQL
- ID: Sequence-based (`BIGINT` via `@SequenceGenerator`)
- Auth: JWT (Bearer token)
- Logging: SLF4J + Logback
- Pagination: Required for all list endpoints

## Architecture
- Layered monolithic (Controller → Service → Repository → DB)
- Soft deletes using `is_active` flag
- Stateless application instances
- Container: Docker
- Default config lives in `src/main/resources/application.yaml` for all profiles

## Logging
- Default logging is configured in `application.yaml`
- Uses structured JSON console output
- Includes MDC fields: `requestId`, `userId`, `method`, `path`
- File logging enabled with rolling policy under `logs/lms-core.log`
- Application name is configured via `spring.application.name` (`lms-core`) and used in startup log messages

## Controller Layer
- `CourseController` added at `src/main/java/com/aditya/lms/controller/CourseController.java`
- Controllers implement OpenAPI-generated APIs:
  - `CourseController` → `com.lms.api.CoursesApi`
  - `ModuleController` → `com.lms.api.ModulesApi`
  - `LessonController` → `com.lms.api.LessonsApi`
  - `EnrollmentController` → `com.lms.api.EnrollmentsApi`
  - `ProgressController` → `com.lms.api.ProgressApi`
- Base mapping uses `@RequestMapping("/api/v1")` to align with server prefix in `openapi.yaml`
- All controller methods are currently stubbed with empty implementations (`return null;`) and no service logic
- Use explicit imports only (no wildcard imports like `com.lms.model.*`) across controllers

## Entity Layer

### Table Naming
- Singular snake_case table names: `course`, `module`, `lesson`, `enrollment`, `progress`

### Audit Fields
- All business entities (`course`, `module`, `lesson`) include: `createdAt`, `createdBy`, `updatedAt`, `updatedBy`
- `createdAt` uses `@CreationTimestamp`, `updatedAt` uses `@UpdateTimestamp`
- `createdBy` / `updatedBy` are `Long` (user IDs), manually set by service layer

### Key Entity Changes (Aug 2026)
- `Course`: fields renamed to `title`, `description`, `tags`; table = `course`; has `@Version`
- `Module`: fields renamed to `title`, `description`; table = `module`; title max 50, description max 100; has `@Version`
- `Lesson`: removed `userId`, `lessonStatus`; fixed `@JoinColumn` on `module`; table = `lesson`; has `@Version`
- `LessonStatus` enum no longer used in `Lesson` entity — moved to `Progress`
- `Progress`: removed `moduleId`, `courseId`, `isActive` (not in schema); retains `@Version`

### Optimistic Locking
- `@Version private Long version` is present on `Course`, `Module`, `Lesson`
- `version` is a Hibernate-managed column — **not documented in schema.md** (it's a JPA concern, not a business schema column)
- Spring Data Envers considered but **not yet implemented** — no `@Audited` annotations applied


✅ **COMPLETED** - Production-ready OpenAPI 3.1 spec generated
- Location: `src/main/resources/openapi.yaml`
- Format: OpenAPI 3.1 (compatible with OpenAPI Generator v7.4.0)
- All 5 resources fully documented: Courses, Modules, Lessons, Enrollments, Progress
- DTOs: 30 generated classes (CreateXRequest, UpdateXRequest, XResponse)
- API Interfaces: 5 generated interfaces (CoursesApi, ModulesApi, LessonsApi, EnrollmentsApi, ProgressApi)
- Jakarta EE: Post-processing fixes applied for javax→jakarta imports
- Code Generation: ✅ Successful (2.4s)
- Compilation: ✅ Successful (clean compile with antrun post-processor)

## Build Configuration
- OpenAPI Generator Maven Plugin: v7.4.0
- Generator: Spring (interface-only mode)
- Post-processing: Antrun plugin converts javax → jakarta imports after generation
- Dependencies added: jakarta-validation-api, hibernate-validator, jackson-databind-nullable, spring-boot-starter-validation

## Generated Classes Summary
- Models: ApiListResponse, CourseResponse, CreateCourseRequest, UpdateCourseRequest, ModuleResponse, LessonResponse, EnrollmentResponse, ProgressResponse, ErrorResponse, and response wrapper classes
- All DTOs use Lombok annotations (@Data, @Builder, @AllArgsConstructor, @NoArgsConstructor)
- All DTOs include Jakarta Bean Validation annotations
- All API interfaces include Spring @RestController annotations
