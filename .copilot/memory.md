# Project Memory

## System & Context Rules
- Always read and analyze .copliot/context files in the active workspace before generating code or answers.

## Tech Stack
- Database: PostgreSQL
- ID: Sequence-based (`BIGINT` via `@SequenceGenerator`)
- Auth: JWT (Bearer token)
- Logging: SLF4J
- Pagination: Required for all list endpoints

## Architecture
- Layered monolithic (Controller → Service → Repository → DB)
- Soft deletes using `is_active` flag
- Stateless application instances
- Container: Docker

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
