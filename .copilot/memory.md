# Project Memory

## System & Context Rules
- Always read and analyze .copliot/context files in the active workspace before generating code or answers.

## Tech Stack
- Database: PostgreSQL
- ID: UUID
- Auth: JWT (Bearer token)
- Logging: SLF4J
- Pagination: Required for all list endpoints

## Architecture
- Layered monolithic (Controller → Service → Repository → DB)
- Soft deletes using `is_active` flag
- Stateless application instances
- Container: Docker

## OpenAPI Specification Status
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
