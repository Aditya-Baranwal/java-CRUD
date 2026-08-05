# System Architecture Specification

## 1. System Overview
<!-- High-level description of what the system does and its core purpose -->
*   **Purpose**: [e.g., A multi-tenant hotel booking and property management platform]
*   **Target Audience**: [e.g., Hotel administrators and global travelers]
*   **Deployment Model**: [e.g., Cloud-native, microservices / monolithic MVC]

## 2. Core Tech Stack
<!-- Explicitly declare the technologies used to guide AI code generation -->
*   **Backend**: Java 21, Spring Boot 3.x, Spring Data JPA
*   **Database**: PostgreSQL 16
*   **Caching/Queue**: Redis (Session management and API caching)
*   **Authentication**: Spring Security + OAuth2 / JWT

## 3. Data & Relationship Models
<!-- Define structural patterns, naming conventions, and relationship ownership -->
### Database Conventions
*   **Naming**: Snake_case for database columns, camelCase for Java entity fields.
*   **Primary Keys**: UUIDv7 preferred for distributed sorting, or standard `BigSerial`.

### Core Entity Relationships (JPA/Hibernate)
*   **Relationship Ownership**: The entity containing the physical foreign key column (`@JoinColumn`) is the owner.
*   **Fetching Strategy**:
    *   All `@ManyToOne` and `@OneToOne` associations **must** explicitly use `fetch = FetchType.LAZY`.
    *   Eager loading is strictly forbidden to prevent N+1 query problems.
*   **Bidirectional Synchronization**: Non-owning sides (`@OneToMany(mappedBy = "...")`) must use defensive helper methods to keep both sides synchronized in memory.

## 4. Architectural Layers & Boundaries
<!-- Define how data moves vertically through the application -->
