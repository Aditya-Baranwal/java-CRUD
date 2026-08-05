# Project Memory

## Tech Stack
- Database: PostgreSQL
- ID: UUID
- Auth: JWT (Bearer token)
- Logging: SLF4J
- Pagination: Required for all list endpoints

## API Specification
- **Spec File**: `src/main/resources/openapi.yaml` (OpenAPI 3.0.3) ✅
- **Version**: /api/v1
- **Base Response Format**: Standard wrapper with message, data, timestamp
- **Endpoints**: Courses, Modules, Lessons, Enrollments, Progress (5 resources)
- **Authorization**: Bearer JWT token

## OpenAPI Integration (Updated: 2026-08-05)
- **Maven Dependency**: `springdoc-openapi-starter-webmvc-ui v2.1.0`
  - Swagger UI: `http://localhost:8081/swagger-ui.html`
  - API Docs: `http://localhost:8081/v3/api-docs`
- **Generator Plugin**: `openapi-generator-maven-plugin v7.4.0`
  - Generates DTOs in `com.lms.model` package
  - Generates controller interfaces in `com.lms.api` package
  - Config: useLombokAnnotations, generateApis, generateModels enabled
  - Generated sources path: `target/generated-sources/openapi/`
- **Makefile**: Available with commands:
  - `make generate-api` - Generate DTOs and interfaces from openapi.yaml
  - `make build` - Build project with generation
  - `make run` - Start application
  - `make test` - Run tests
  - `make all` - Full pipeline (clean, install, generate, build)

## Architecture
- Layered monolithic (Controller → Service → Repository → DB)
- Soft deletes using `is_active` flag
- Stateless application instances
- Container: Docker
