# Project Memory

## System & Context Rules
- Always read and analyze .copliot/context files in the active workspace before generating code or answers.

## Tech Stack
- Database: PostgreSQL
- ID: Sequence-based (`BIGINT` via `@SequenceGenerator`)
- Auth: JWT (******
- Logging: SLF4J + Logback
- Pagination: Required for all list endpoints
- Course tags are mapped as `List<String>` and stored in PostgreSQL `text[]`.

## Architecture
- Layered monolithic (Controller → Service → Repository → DB)
- Soft deletes using `is_active` flag
- Stateless application instances
- Container: Docker
- Default config lives in `src/main/resources/application.yaml` for all profiles
- Liquibase is enabled by default in `application.yaml` via `spring.liquibase.enabled: true`

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
- `CourseController` is wired to `CourseService` and does DTO↔Entity mapping via `CourseMapper`
- Other controllers are still stubbed with empty implementations (`return null;`)
- Use explicit imports only (no wildcard imports like `com.lms.model.*`) across controllers

## Service Layer
- `CourseService` and `CourseServiceImpl` are implemented
- Service contract is entity-based (`Course` as input/output), not DTO-based
- Business rules enforced:
  - duplicate active course title per instructor is not allowed
  - course instructor cannot be changed during update
  - soft delete sets `isActive=false`
  - create requires mandatory fields: `title` and `instructorId`

## Exception Handling
- Added base domain exception: `BaseException` with `errorCode` and `HttpStatus`
- Added course exceptions:
  - `CourseNotFoundException` (`COURSE_404`)
  - `CourseConflictException` (`COURSE_409`)
  - `CourseValidationException` (`COURSE_400`)
- Added `GlobalExceptionHandler` (`@RestControllerAdvice`) returning `ErrorResponseDTO`

## OpenAPI Updates
- `CreateCourseRequest` now requires only:
  - `courseTitle`
  - `instructorId`

## Database Migration
- Liquibase changelog is configured at `classpath:db/changelog/db.changelog-master.yaml`
- Master changelog currently includes:
  - `000-create-user-table.yaml`
  - `001-create-course-table.yaml`
  - `002-create-module-table.yaml`
  - `003-create-lesson-table.yaml`
  - `004-create-enrollment-table.yaml`
  - `005-create-progress-table.yaml`
- Changelog includes are set with `relativeToChangelogFile: true` in master file to avoid include-path parsing issues.
- `000-create-user-table.yaml` defines PostgreSQL enum `user_role` (`ADMIN`, `INSTRUCTOR`, `USER`) and uses it as the `user.role` column type (not `VARCHAR`).
- `001-create-course-table.yaml` defines `course_id_seq` and uses PostgreSQL `text[]` for `course.tags` with default `'{}'::text[]`.
- `002-create-module-table.yaml` defines `module_id_seq`, FK to `course`, and unique `(course_id, sequence)` constraint.
- `003-create-lesson-table.yaml` defines `lesson_id_seq`, PostgreSQL enum `content_type`, and unique `(module_id, sequence)` constraint.
- `004-create-enrollment-table.yaml` defines `enrollment_id_seq`, PostgreSQL enum `course_completion_status`, unique `(user_id, course_id)`, and `version` column.
- `005-create-progress-table.yaml` defines `progress_id_seq`, PostgreSQL enum `lesson_status`, unique `(user_id, lesson_id)`, and `version` column.
- `Course.tags` is modeled as `List<String>` in Java and persisted as PostgreSQL `text[]` with default `[]`.
- `Enrollment.courseCompletionStatus` uses the `CourseCompletionStatus` enum and maps to the PostgreSQL column `course_completion_status`.

## OpenAPI Design Conventions
- Reusable shared schemas live in `src/main/resources/openapi/common.yaml`; main spec references them via relative paths such as `./openapi/common.yaml#/components/schemas/ModuleSummary`.
- Schema names are entity-first and consistent, e.g. `CourseCreateRequest`, `CourseUpdateRequest`, `CourseGetResponse`, `CourseListResponse`, `ModuleCreateRequest`, `ModuleUpdateRequest`, `LessonCreateRequest`, `LessonUpdateRequest`, `EnrollmentCreateRequest`, `ProgressUpdateRequest`.
- List endpoints use dedicated list response schemas (`CourseListResponse`, `ModuleListResponse`, `LessonListResponse`, `EnrollmentListResponse`, `ProgressListResponse`) and do not include nested child collections in the list payloads.
- Nested child collections remain only in detail responses (`CourseResponse.modules`, `ModuleResponse.lessons`), not in list responses.
- Array response fields define `default: []` to reflect empty-array semantics in the API contract and match PostgreSQL array defaults.
- Common enums and summary schemas such as `ContentType`, `LessonStatus`, `CourseCompletionStatus`, `ModuleSummary`, `LessonSummary`, `ErrorResponse`, and list wrappers should be shared rather than redefined inline.

## Entity Layer

### Table Naming
- Singular snake_case table names: `course`, `module`, `lesson`, `enrollment`, `progress`

### Audit Fields
- All business entities (`course`, `module`, `lesson`, `enrollment`, `progress`) include the project-standard audit/version behavior relevant to each table
- `createdAt` uses `@CreationTimestamp`, `updatedAt` uses `@UpdateTimestamp` where applicable
- `createdBy` / `updatedBy` are `Long` (user IDs), manually set by the service layer for course/module/lesson tables
- `Enrollment` and `Progress` follow the schema-specific columns and carry the `version` field for optimistic locking

### Key Entity Changes (Aug 2026)
- `Course`: fields renamed to `title`, `description`, `tags`; table = `course`; has `@Version`
- `Module`: fields renamed to `title`, `description`; table = `module`; title max 50, description max 100; has `@Version`
- `Lesson`: removed `userId`, `lessonStatus`; fixed `@JoinColumn` on `module`; table = `lesson`; has `@Version`
- `Enrollment`: renamed enum usage to `CourseCompletionStatus`; column is `course_completion_status`; has `@Version`
- `Progress`: enum remains `LessonStatus`; retains `@Version`
- `LessonStatus` enum no longer used in `Lesson` entity — moved to `Progress`

### Optimistic Locking
- `@Version private Long version` is present on `Course`, `Module`, `Lesson`, `Enrollment`, and `Progress`
- `version` is a Hibernate-managed column and is part of the migration schema for these tables
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
