# SQL Development Skill

> **Skill Name**
>
> Production-Grade SQL & Database Development

---

# Purpose

This skill defines how an AI agent should design, write, review, optimize, and troubleshoot SQL for production systems.

The AI should generate SQL that is

- Correct
- Performant
- Scalable
- Maintainable
- Secure
- Database-independent where practical

The AI should always think like a Senior Database Engineer.

---

# Supported Databases

Default

```
PostgreSQL
```

Also support

- Microsoft SQL Server
- MySQL
- Oracle
- MariaDB

If the project already uses a specific database,

follow its syntax and capabilities.

---

# SQL Philosophy

Always prioritize

- Correctness
- Simplicity
- Performance
- Readability
- Data Integrity

Never optimize before understanding the query.

---

# Before Writing SQL

Understand

- Business requirement
- Table relationships
- Existing indexes
- Expected data volume
- Transaction boundaries
- Isolation requirements

Never guess table structures.

---

# Query Design

Prefer

- Explicit joins
- Meaningful aliases
- Parameterized queries
- Readable formatting

Avoid

- Nested queries when joins are clearer
- SELECT *
- Unnecessary DISTINCT

---

# SELECT Queries

Always select only required columns.

Good

```sql
SELECT
    id,
    name,
    email
FROM users;
```

Avoid

```sql
SELECT *
FROM users;
```

---

# WHERE Clause

Use indexed columns whenever possible.

Prefer

```sql
WHERE id = ?
```

instead of

```sql
WHERE LOWER(name) = LOWER(?)
```

when indexes can be used.

Avoid wrapping indexed columns inside functions.

---

# JOIN Strategy

Prefer

```
INNER JOIN
```

when relationship is mandatory.

Use

```
LEFT JOIN
```

only when optional data is required.

Always specify join conditions explicitly.

Bad

```sql
FROM A, B
```

Good

```sql
FROM A
JOIN B
ON A.id = B.id
```

---

# Aggregations

Use

```
GROUP BY

HAVING
```

correctly.

Avoid grouping unnecessary columns.

Prefer database aggregation instead of application-side aggregation.

---

# Window Functions

Use window functions when appropriate.

Examples

```
ROW_NUMBER()

RANK()

DENSE_RANK()

LAG()

LEAD()

SUM() OVER()
```

Avoid replacing them with inefficient self joins.

---

# Pagination

Preferred

```
LIMIT
OFFSET
```

or

```
FETCH NEXT
```

For very large datasets,

prefer

```
Keyset Pagination
```

instead of large OFFSET values.

---

# Sorting

Always sort explicitly.

Example

```sql
ORDER BY created_at DESC
```

Never rely on implicit ordering.

---

# EXISTS vs IN

Prefer

```
EXISTS
```

for correlated lookups.

Use

```
IN
```

for small static lists.

Avoid

```
NOT IN
```

when NULL values are possible.

Prefer

```
NOT EXISTS
```

---

# UNION

Use

```
UNION ALL
```

unless duplicate removal is required.

Avoid

```
UNION
```

without necessity.

---

# Common Table Expressions (CTE)

Prefer

```
WITH
```

for readability.

Example

```sql
WITH active_users AS (...)
SELECT ...
```

Avoid deeply nested subqueries.

---

# Transactions

Always understand

- BEGIN
- COMMIT
- ROLLBACK

Never leave transactions open.

Keep transactions short.

---

# Isolation Levels

Understand

- Read Uncommitted
- Read Committed
- Repeatable Read
- Serializable

Recommend the lowest level that satisfies consistency requirements.

---

# Locking

Understand

- Shared Locks
- Exclusive Locks
- Row Locks
- Table Locks

Avoid long-running locks.

Use locking only when necessary.

---

# Indexing

Recommend indexes on

- Primary Keys
- Foreign Keys
- Frequently filtered columns
- Frequently joined columns
- Frequently sorted columns

Avoid indexing

- Low-cardinality columns
- Rarely queried columns

---

# Query Optimization

Review

- Execution Plan
- Index usage
- Table scans
- Nested loops
- Hash joins
- Merge joins

Always analyze execution plans before optimizing.

---

# Performance Rules

Avoid

- SELECT *
- Cartesian joins
- Unbounded queries
- Functions on indexed columns
- Repeated subqueries
- Correlated subqueries when joins are better

Prefer

- Covering indexes
- EXISTS
- Batch operations
- Window functions
- Proper filtering

---

# Data Integrity

Always respect

- Primary Keys
- Foreign Keys
- Unique Constraints
- Check Constraints

Never rely solely on application validation.

---

# Inserts

Prefer

Batch inserts.

Example

```sql
INSERT INTO users (...)
VALUES (...),
       (...),
       (...);
```

Avoid inserting one row at a time when bulk operations are possible.

---

# Updates

Always include

```
WHERE
```

unless intentionally updating every row.

Never generate

```sql
UPDATE users
SET status = 'ACTIVE';
```

without explicit confirmation.

---

# Deletes

Always include

```
WHERE
```

Prefer soft delete if project supports it.

Never delete production data without confirmation.

---

# Stored Procedures

Generate procedures only when

- Required by project
- Performance-critical
- Database-specific logic exists

Avoid unnecessary business logic in procedures.

---

# Views

Use views for

- Reporting
- Simplifying complex queries
- Read-only abstractions

Avoid deeply nested views.

---

# Security

Always use parameterized queries.

Never concatenate user input.

Bad

```sql
"... WHERE id = " + userId
```

Good

Prepared statements.

Protect against SQL Injection.

---

# SQL Review Checklist

Review

- Index usage
- Query readability
- Join correctness
- Transaction scope
- Locking
- Performance
- Security
- Scalability

---

# Troubleshooting

When debugging SQL

Check

1. Execution Plan
2. Missing Indexes
3. Blocking Sessions
4. Deadlocks
5. Lock Waits
6. Statistics
7. Fragmentation
8. Query Cache
9. Cardinality Estimates

---

# SQL Anti-Patterns

Never generate

- SELECT *
- Missing WHERE in UPDATE
- Missing WHERE in DELETE
- Functions on indexed columns
- N+1 queries
- Repeated correlated subqueries
- Unbounded ORDER BY
- Implicit joins
- String concatenated SQL
- Duplicate queries

---

# Database-Specific Notes

## PostgreSQL

Prefer

- CTEs
- JSONB
- RETURNING
- EXPLAIN ANALYZE

---

## SQL Server

Prefer

- OFFSET FETCH
- OUTPUT clause
- MERGE only when justified
- Proper clustered indexes

---

## MySQL

Prefer

- LIMIT
- EXPLAIN
- InnoDB
- Composite indexes

Avoid MyISAM.

---

# AI Self Validation

Before returning SQL verify

✓ Correct syntax

✓ Parameterized query

✓ No SELECT *

✓ Proper joins

✓ Index-friendly predicates

✓ WHERE clause present for UPDATE/DELETE

✓ Pagination considered

✓ Security considered

✓ Readability acceptable

✓ Production-ready

---

# Definition of Done

SQL is complete only when

- Syntax correct
- Query optimized
- Indexes considered
- Execution plan reviewed (where applicable)
- Transactions correct
- Security verified
- Data integrity preserved
- Scalable for expected volume
- Ready for production

---

# Example Invocations

### Generate Query

```
Find available hotel rooms between two dates.
```

The AI should

- Analyze schema
- Generate optimized SQL
- Explain indexing strategy
- Discuss performance considerations

---

### Review SQL

```
Review booking availability query.
```

The AI should verify

- Correctness
- Index usage
- Join efficiency
- Locking implications
- Execution plan considerations
- Suggested optimizations

---

### Optimize Query

```
Optimize slow customer search query.
```

The AI should

- Identify bottlenecks
- Recommend indexes
- Rewrite SQL if beneficial
- Explain execution plan improvements
- Preserve query behavior