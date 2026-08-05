# Project Memory

## Tech Stack
- Database: PostgreSQL
- ID: UUID
- Auth: JWT (Bearer token)
- Logging: SLF4J
- Pagination: Required for all list endpoints

## API Specification
- **Generated**: open-api.yaml (OpenAPI 3.0.3) ✅
- **Version**: /api/v1
- **Base Response Format**: Standard wrapper with message, data, timestamp
- **Endpoints**: Courses, Modules, Lessons, Enrollments, Progress (5 resources)
- **Authorization**: Role-based (ADMIN, INSTRUCTOR, USER)

## Architecture
- Layered monolithic (Controller → Service → Repository → DB)
- Soft deletes using `is_active` flag
- Stateless application instances
- Container: Docker
