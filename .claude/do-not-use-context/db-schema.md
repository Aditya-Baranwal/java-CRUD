# Database Schema Context

> Purpose
>
> This document provides AI agents and developers with a high-level understanding
> of the database design used by the system.
>
> It intentionally avoids detailed DDL statements.
>
> Table definitions, migrations, indexes, and constraints are maintained in
> **docs/database.md**.
>
> This document explains the purpose of each entity, relationships,
> ownership, lifecycle, design principles, and implementation expectations.

---

# 1. Database Philosophy

The database is the source of truth for all persistent business data.

Design goals

- Data Integrity
- Normalized schema
- Referential Integrity
- Scalability
- Maintainability
- Performance
- Auditability

---

# 2. Database Engine

Recommended

```
PostgreSQL
```

Supported

- PostgreSQL
- MySQL
- Microsoft SQL Server

ORM

```
Spring Data JPA
```

Migration Tool

```
Flyway
```

---

# 3. Schema Organization

Business Tables

```
user
course
module
lesson
enrollment
progress
```

Reference Tables

```
country
state
city
```

Audit Tables

```
audit_log
```

Configuration Tables

```
application_config
```

---

# 4. Primary Key Strategy

Every table must have

```
BIGINT
```

Generated using

- Identity
- Sequence
- UUID (distributed systems)

Primary keys are immutable.

---

# 5. Foreign Key Strategy

Relationships must use foreign keys.

Example

```
module.course_id
        ↓
course.id
```

Never store duplicated business information when a relationship exists.

---

# 6. Relationship Guidelines

Supported

- One-to-One
- One-to-Many
- Many-to-One
- Many-to-Many (through junction tables)

Avoid direct many-to-many relationships without an explicit join entity.

---

# 7. Table Ownership

| Table | Business Owner |
|--------|----------------|
| user | Identity Service |
| course | Course Service |
| module | Course Service |
| lesson | Course Service |
| enrollment | Enrollment Service |
| progress | Progress Service |

---

# 8. Naming Convention

Tables

```
snake_case
```

Examples

```
course

course_module

user_progress
```

Columns

```
snake_case
```

Examples

```
created_at

updated_at

course_title
```

Constraints

```
pk_course

fk_module_course

uk_email
```

Indexes

```
idx_course_title

idx_user_email
```

---

# 9. Standard Columns

Every business table should contain

```
id

created_at

updated_at

created_by

updated_by
```

Optional

```
deleted_at

deleted_by

version
```

---

# 10. Audit Strategy

Track

- Created Time
- Updated Time
- Created By
- Updated By

Optional

Soft Delete

```
deleted_at
deleted_by
```

---

# 11. Soft Delete

Preferred

Instead of

```
DELETE
```

Use

```
is_active

or

deleted_at
```

Business decides whether hard delete is allowed.

---

# 12. Optimistic Locking

Use

```
version
```

for concurrent updates.

Avoid pessimistic locking unless required.

---

# 13. Transaction Rules

A transaction should

- Begin in Service Layer
- End in Service Layer

Repositories should not manage transactions.

---

# 14. Normalization

Target

Third Normal Form (3NF)

Denormalization is allowed only after performance analysis.

---

# 15. Index Strategy

Create indexes for

- Foreign Keys
- Frequently searched columns
- Frequently sorted columns
- Frequently filtered columns

Avoid indexing

- Low cardinality columns
- Rarely queried columns

---

# 16. Unique Constraints

Use unique constraints for

- Email
- Username
- External Identifier

Never rely solely on application validation.

---

# 17. Check Constraints

Use database constraints whenever possible.

Examples

```
age > 0

price >= 0

quantity >= 0
```

---

# 18. Enum Strategy

Preferred

Reference tables

Alternative

Database ENUM

Application enums must remain synchronized with database values.

---

# 19. JSON Columns

Allowed only for

- Flexible Metadata
- User Preferences
- Configuration

Avoid storing searchable business data inside JSON.

---

# 20. Large Objects

Store

- Images
- Videos
- Documents

outside database.

Persist only

```
URL

Object Key

Metadata
```

---

# 21. Cascade Rules

Preferred

```
RESTRICT
```

Avoid

```
ON DELETE CASCADE
```

unless explicitly required.

Business logic should control deletion.

---

# 22. Read vs Write Tables

Write

Normalized schema

Read

Optimized queries

Materialized Views (optional)

Read replicas for reporting.

---

# 23. Query Guidelines

Prefer

```
SELECT required_columns
```

Avoid

```
SELECT *
```

Batch reads when possible.

Avoid N+1 queries.

---

# 24. Pagination

Every list query must support

```
LIMIT

OFFSET
```

or

```
Keyset Pagination
```

for large datasets.

---

# 25. Performance Guidelines

Avoid

- Table scans
- Cartesian joins
- Nested loops over huge datasets
- Large transactions

Prefer

- Batch Inserts
- Bulk Updates
- Indexed lookups

---

# 26. Data Integrity

Database should enforce

- Primary Keys
- Foreign Keys
- Unique Constraints
- Check Constraints

Business validation complements—not replaces—database constraints.

---

# 27. Security

Never store

- Plain text passwords
- API Secrets
- Tokens

Sensitive data should be

- Hashed
- Encrypted where necessary

PII should follow compliance requirements.

---

# 28. Backup Strategy

Support

- Daily Full Backup
- Incremental Backup
- Point-in-Time Recovery

Backup verification should be automated.

---

# 29. Migration Guidelines

Every schema change must be versioned.

Use

```
Flyway
```

Migration rules

- Forward only
- Never edit executed migrations
- Roll-forward preferred over rollback

---

# 30. AI Implementation Rules

AI agents must

- Reuse existing tables before creating new ones.
- Search for existing relationships.
- Avoid duplicate entities.
- Preserve naming conventions.
- Add indexes for new foreign keys.
- Update ER diagrams after schema changes.
- Create Flyway migrations for every structural change.
- Never remove columns without a migration strategy.

---

# 31. Database Quality Checklist

Every schema change must verify

- Primary Key
- Foreign Keys
- Indexes
- Constraints
- Audit Columns
- Naming Convention
- Performance Impact
- Migration Script
- Roll-forward Compatibility

---

# 32. Future Enhancements

Potential additions

- Read Replicas
- Partitioned Tables
- Materialized Views
- Event Store
- Outbox Pattern
- CDC (Change Data Capture)
- Multi-Tenant Schema
- Archive Database

---

# 33. Related Documents

| Document | Purpose |
|----------|---------|
| docs/database.md | Complete DDL and schema |
| docs/er-diagram.md | Entity relationship diagrams |
| context/domain.md | Domain model |
| context/api-spec.md | API conventions |
| context/performance.md | Database performance expectations |
| context/security.md | Database security standards |
| context/business-rules.md | Business invariants affecting persistence |

---

# 34. Scope

This document intentionally excludes

- CREATE TABLE statements
- ALTER TABLE statements
- Migration scripts
- Stored procedures
- Triggers
- SQL queries
- ORM mappings

These belong in

```
docs/database.md
```

or the project's migration files.