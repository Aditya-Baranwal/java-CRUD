# Architectural Specification

Defines the architectural rules for the Learning Management System (LMS).

Detailed specifications are maintained under `docs/`.

---

# Source of Truth

The following documents are authoritative.

- docs/requirements.md
- docs/architecture.md
- docs/database.md
- docs/schema.md
- docs/api.md
- docs/security.md
- docs/deployment.md

If conflicts exist, documents inside `docs/` take precedence.

---

# Architecture Style

Layered monolithic application.

```
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Application instances are stateless.

Containers are independently deployable.

---

# Package Structure

```
controller/
service/
repository/
entity/
enums/
dto/
mapper/
config/
security/
exception/
validation/
util/
```

---

# Dependency Rules

Allowed

```
Controller → Service

Service → Repository

Repository → Database
```

Not Allowed

```
Controller → Repository

Repository → Service

Entity → Controller

DTO → Repository
```

---

# Layer Responsibilities

| Layer | Responsibility |
|---------|---------------|
| Controller | HTTP |
| Service | Business Logic |
| Repository | Persistence |
| Entity | Database Mapping |
| DTO | API Contract |
| Mapper | Object Conversion |

---

# API Rules

- REST APIs
- Resource-oriented endpoints
- No nested paths
- Query parameter filtering
- Pagination support
- Standard response wrapper

Example

```
GET /courses

GET /modules?courseId=1

GET /lessons?moduleId=1
```

Avoid

```
GET /courses/1/modules
```

---

# Database Rules

- PostgreSQL is source of truth.
- Repositories own persistence.
- Service layer owns transactions.
- Soft delete using `is_active`.
- Audit columns on all entities.

See:

- docs/database.md
- docs/schema.md

---

# Caching Rules

Cache candidates:

- Course
- Module
- Lesson

Avoid caching:

- Enrollment
- Progress

Redis is the approved cache.

---

# Security Rules

- JWT Authentication
- Role-based Authorization
- Bcrypt Password Hashing

See:

- docs/security.md

---

# Deployment Rules

Application must remain:

- Stateless
- Containerized
- Externally configured

See:

- docs/deployment.md

---

# Design Principles

- SOLID
- DRY
- KISS
- Separation of Concerns

---

# AI Context Usage

For implementation details consult:

Business Rules

```
context/business-domain.md
```

Coding Standards

```
context/coding-standards.md
```

Detailed Design

```
docs/
```