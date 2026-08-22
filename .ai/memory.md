# Project Memory

Database: PostgreSQL
ID: Sequence-based BIGINT
Auth: JWT
Logging: SLF4J + Logback
Pagination required.

OpenAPI conventions:
- Use shared schemas in `src/main/resources/openapi/common.yaml`.
- Use entity-first schema names like `CourseCreateRequest`, `CourseUpdateRequest`, `CourseListResponse`.
- Keep list responses separate from detail responses; lists do not include child collections.
- Default array fields to `[]` in the API schema.
- `Course.tags` is `List<String>` in Java and persisted as PostgreSQL `text[]`.
