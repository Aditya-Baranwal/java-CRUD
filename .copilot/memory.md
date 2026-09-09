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
- Soft deletes use the course `can_enrollment` flag to disable enrollment while preserving the course record
- Stateless application instances
- Container: Docker
- Default config lives in `src/main/resources/config/application.yaml` and profile overrides live alongside it
- Liquibase is enabled by default in `config/application.yaml` via `spring.liquibase.enabled: true`

## Logging
- Default logging is configured in `config/application.yaml`
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
- `CourseController`, `ModuleController`, `LessonController`, `EnrollmentController`, and `ProgressController` are wired to services plus plain Spring `@Component` mappers for DTO↔Entity mapping
- Use explicit imports only (no wildcard imports like `com.lms.model.*`) across controllers

## Service Layer
- `CourseService` and `CourseServiceImpl` are implemented.
- Service contract is entity-based (`Course` as input/output), not DTO-based.
- Business rules enforced:
  - duplicate course title per instructor is rejected with a conflict error
  - course instructor cannot be changed during update
  - create only allows `DRAFT` as initial state and defaults `canEnrollment=false`
  - `can_enrollment` is only valid when `course_status = PUBLISHED`
  - terminal states block edit/delete transitions
  - only `PUBLISHED` and `PLANNED_TO_UNPUBLISH` are student-visible
  - student lists are filtered by `course_status`, not enrollment or `can_enrollment`
  - soft delete sets `canEnrollment=false` and moves the course to `MANUAL_UNPUBLISHED`

- `LessonService` / `LessonServiceImpl` enforce the rules in `docs/decisions/module.decisions.md`, applied one level deeper via `Lesson -> Module -> Course`:
  - Lesson create/update/delete is only allowed while the owning course's `courseStatus` is `DRAFT` or `READY_TO_PUBLISH`; blocked once `PUBLISHED` or higher (this single gate also covers toggling `isActive`, since that's a subset of "edit").
  - Listing: admin/instructor see all lessons (active + inactive) only when the course is `DRAFT`; otherwise the `active` filter applies (defaults to `true`) — same as student view.
  - **Role modeling convention**: no `UserRole` enum/parameter is used. Authorization is expressed as **separate methods per caller role** (mirrors the existing `CourseService.listCoursesForAdmin/Instructor/Student` pattern):
    - `createLessonAsAdmin(lesson, adminId)` / `createLessonAsInstructor(lesson, instructorId)`
    - `updateLessonAsAdmin(lessonId, lesson, adminId)` / `updateLessonAsInstructor(lessonId, lesson, instructorId)`
    - `deleteLessonAsAdmin(lessonId, adminId)` / `deleteLessonAsInstructor(lessonId, instructorId)`
    - `listLessonsForAdmin(...)` / `listLessonsForInstructor(moduleId, instructorId, ...)` / `listLessonsForStudent(...)`
  - Students get **no** create/update/delete method at all — unauthorized actions are prevented at compile time, not via a runtime role check.
  - Instructor variants verify `course.instructorId == instructorId` (`ErrorMessages.lessonInstructorForbidden`, thrown as `LessonForbiddenException`, HTTP 403); admin variants skip ownership checks.
  - `LessonController` currently wires all calls to the **admin-variant** methods with a hardcoded `TEMP_REQUESTER_ID = 0L` as a stopgap (marked `TODO`), since there is no authenticated caller identity yet — replace once a security layer exists.
  - There is no `User` entity, `Role` enum, or Spring Security layer anywhere in the codebase yet; any future role/ownership enforcement work needs this foundation first.

- `ModuleService` / `ModuleServiceImpl` enforce the rules in `docs/decisions/module.decisions.md` (applied one level shallower than Lesson, directly via `Module -> Course`) using the **same per-role-method convention** as `LessonService`:
  - Module create/update/delete (including the `isActive` toggle) is only allowed while the owning course's `courseStatus` is `DRAFT` or `READY_TO_PUBLISH`; blocked once `PUBLISHED` or higher.
  - Listing: admin/instructor see all modules (active + inactive) only when the course is `DRAFT`; otherwise the `active` filter applies (defaults to `true`) — same as student view.
  - Methods: `createModuleAsAdmin(module, adminId)` / `createModuleAsInstructor(module, instructorId)`; `updateModuleAsAdmin(moduleId, module, adminId)` / `updateModuleAsInstructor(moduleId, module, instructorId)`; `deleteModuleAsAdmin(moduleId, adminId)` / `deleteModuleAsInstructor(moduleId, instructorId)`; `listModulesForAdmin(...)` / `listModulesForInstructor(courseId, instructorId, ...)` / `listModulesForStudent(...)`.
  - Students get no create/update/delete method; instructor variants verify `course.instructorId == instructorId` via `ModuleForbiddenException` (`MODULE_403`), admin variants skip ownership checks.
  - `ModuleController` wires all calls to the admin-variant methods with the same `TEMP_REQUESTER_ID = 0L` stopgap as `LessonController`, pending a real security layer.

## Exception Handling
- Added base domain exception: `BaseException` with `errorCode` and `HttpStatus`.
- Added course exceptions:
  - `CourseNotFoundException` (`COURSE_404`)
  - `CourseConflictException` (`COURSE_409`)
  - `CourseValidationException` (`COURSE_400`)
- Added lesson exceptions, all supporting an `ErrorMessages.Error`-based constructor in addition to a raw-string fallback:
  - `LessonNotFoundException` (`LESSON_404`)
  - `LessonConflictException` (`LESSON_409`)
  - `LessonValidationException` (`LESSON_400`)
  - `LessonForbiddenException` (`LESSON_403`) — new, added for instructor-ownership violations; there was previously no 403/Forbidden exception class in the project.
- Added module exceptions (same `Error`-record pattern):
  - `ModuleNotFoundException` (`MODULE_404`)
  - `ModuleConflictException` (`MODULE_409`)
  - `ModuleValidationException` (`MODULE_400`)
  - `ModuleForbiddenException` (`MODULE_403`) — new, for instructor-ownership violations, mirrors `LessonForbiddenException`.
- Centralized all reusable error codes/messages in `com.aditya.lms.exception.ErrorMessages` using a shared `Error` record; Lesson-specific codes are `LESSON_001`-`LESSON_014` plus `LESSON_404`; Module-specific codes are `MODULE_001`-`MODULE_014` plus `MODULE_404`.
- Added `GlobalExceptionHandler` (`@RestControllerAdvice`) returning `ErrorResponseDTO`.

## Unit Test Conventions (`.copilot/prompts/unit.test.prompt.md`)
- JUnit 5 + Mockito + AssertJ; `@ExtendWith(MockitoExtension.class)`, `@Mock` for repositories, `@InjectMocks` for the service under test, `@Captor` for `ArgumentCaptor` (e.g. entity captor + `Pageable` captor). Never mock the class under test or mappers/entities.
- Service test classes (e.g. `ModuleServiceImplTest`) group tests per public method with `@Nested` classes (`CreateModule`, `GetModule`, `ListModules`, `UpdateModule`, `DeleteModule`), mirroring `CourseServiceImplTest`. Each nested class covers happy path, validation failures (null/blank/invalid input), business-rule violations (duplicate sequence, non-mutable course state via `@ParameterizedTest @EnumSource`), forbidden/ownership failures, and not-found.
- Mapper test classes (e.g. `ModuleMapperTest`) instantiate the mapper directly (no mocks needed) and group by mapper method (`ToEntity`, `ApplyUpdates`, `ToCreateResponse`, `ToGetResponse`, `ToUpdateResponse`, `ToDeleteResponse`, `ToListResponse`), asserting field mapping, null handling, and collection/page mapping.
- Test fixtures live in `src/test/java/com/aditya/lms/testdata/` as final classes with a private constructor, a `defaultXxxBuilder()` returning the Lombok builder, and convenience static factory methods (e.g. `ModuleTestData.draftModule()`, `moduleWithCourseStatus(status)`, `newUnsavedModule()`), matching `CourseTestData`.
- `ModuleServiceImplTest`/`ModuleMapperTest`/`ModuleTestData` and `LessonServiceImplTest`/`LessonMapperTest`/`LessonTestData` were added following this convention; all mirror the `Course` equivalents 1:1 in structure.

## OpenAPI Updates
- Course lifecycle status is modeled through a dedicated `courseStatus` field and shared enum `CourseStatus`.
- Query filters use `courseStatus` instead of an `active` flag; visibility checks are based on lifecycle status.
- The course enrollment gate is named `canEnrollment` in the API and `can_enrollment` in the database; it is only meaningful when `courseStatus = PUBLISHED`.
- `CourseCreateRequest` and `CourseUpdateRequest` default to `DRAFT` on creation; `canEnrollment` defaults to `false` in the domain model.
- `CourseResponse` and `CourseSummary` include `courseStatus` and `canEnrollment` to reflect lifecycle state and enrollment availability in create/get/list payloads.
- The shared enum values follow the schema contract: `DRAFT`, `READY_TO_PUBLISH`, `PUBLISHED`, `PLANNED_TO_UNPUBLISH`, `READY_TO_UNPUBLISH`, `UNPUBLISHED`, `MANUAL_UNPUBLISHED`.
- `Course.tags` is represented as `courseTags` array with default `[]` in all API schema variants.
- Generated OpenAPI DTOs use enum wrappers like `CourseResponse.CourseStatusEnum` and `CourseListResponseDataInnerDTO.CourseStatusEnum`; mapper code must convert domain enum values to these generated enum types.

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
- `001-create-course-table.yaml` defines `course_id_seq`, `can_enrollment` as the enrollment gate, and uses PostgreSQL `text[]` for `course.tags` with default `'{}'::text[]`.
- `002-create-module-table.yaml` defines `module_id_seq`, FK to `course`, and unique `(course_id, sequence)` constraint.
- `003-create-lesson-table.yaml` defines `lesson_id_seq`, PostgreSQL enum `content_type`, and unique `(module_id, sequence)` constraint.
- `004-create-enrollment-table.yaml` defines `enrollment_id_seq`, PostgreSQL enum `course_completion_status`, unique `(user_id, course_id)`, and `version` column.
- `005-create-progress-table.yaml` defines `progress_id_seq`, PostgreSQL enum `lesson_status`, unique `(user_id, lesson_id)`, and `version` column.
- `Course.tags` is modeled as `List<String>` in Java and persisted as PostgreSQL `text[]` with default `[]`.
- `Course.courseStatus` uses the `CourseStatus` enum and maps to PostgreSQL column `course_status` with default `DRAFT`.
- `Course.canEnrollment` defaults to `false` and is treated as the enrollment gate only when the lifecycle status is `PUBLISHED`.
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
- `Course`: fields renamed to `title`, `description`, `tags`; adds `courseStatus` enum field mapped to PostgreSQL `course_status`; adds `canEnrollment` gate mapped to PostgreSQL `can_enrollment`; table = `course`; has `@Version`
- `Module`: fields renamed to `title`, `description`; table = `module`; title max 50, description max 100; has `@Version`
- `Lesson`: removed `userId`, `lessonStatus`; fixed `@JoinColumn` on `module`; table = `lesson`; has `@Version`
- `Enrollment`: renamed enum usage to `CourseCompletionStatus`; column is `course_completion_status`; has `@Version`
- `Progress`: enum remains `LessonStatus`; retains `@Version`
- `LessonStatus` enum no longer used in `Lesson` entity — moved to `Progress`
- `CourseStatus` enum is a domain enum separate from `CourseCompletionStatus`; it is used only for course lifecycle and stored in the `course_status` column.

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
