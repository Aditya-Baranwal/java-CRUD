# System Architecture

This document describes the overall architecture of the Learning Management System (LMS), including application layers, deployment model, technology stack, request lifecycle, caching strategy, and scalability considerations.

---

# Table of Contents

- Architecture Overview
- Design Goals
- Technology Stack
- High-Level Architecture
- Application Architecture
- Application Layers
- Component Responsibilities
- Request Lifecycle
- Authentication Flow
- Caching Architecture
- Database Architecture
- Deployment Architecture
- Containerization
- Scalability
- Reliability
- Logging
- Monitoring
- Design Principles
- Future Enhancements
- References

---

# Architecture Overview

The Learning Management System (LMS) is implemented as a stateless RESTful backend application using Spring Boot.

The application provides APIs for

- User Management
- Course Management
- Module Management
- Lesson Management
- Course Enrollment
- Learning Progress Tracking

Authentication is performed using JWT.

The application is packaged as a Docker container and can be deployed on any container runtime.

---

# Design Goals

The architecture is designed with the following goals.

- Separation of Concerns
- Low Coupling
- High Cohesion
- Stateless Services
- Easy Deployment
- Container Friendly
- Maintainability
- Scalability
- Testability
- Secure by Default

---

# Technology Stack

| Layer | Technology |
|--------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| Build Tool | Maven |
| Security | Spring Security + JWT |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Cache | Redis |
| API Specification | OpenAPI 3.1 |
| API Documentation | Swagger UI |
| Containerization | Docker |
| Reverse Proxy *(Optional)* | NGINX |
| Logging | SLF4J + Logback |
| Testing | JUnit 5 + Mockito |

---

# High-Level Architecture

```text
                     +----------------------+
                     |  Web / Mobile Client |
                     +----------+-----------+
                                |
                             HTTPS
                                |
                                v
                     +----------------------+
                     |   NGINX (Optional)   |
                     +----------+-----------+
                                |
                                |
                                v
                +-------------------------------+
                |     LMS Spring Boot Service   |
                |         Docker Container      |
                +---------------+---------------+
                                |
                +---------------+---------------+
                |                               |
                v                               v
         +---------------+             +----------------+
         |     Redis     |             |   PostgreSQL   |
         +---------------+             +----------------+
```

---

# Application Architecture

The application follows a layered architecture.

```text
Controller
     │
     ▼
Service
     │
     ▼
Repository
     │
     ▼
PostgreSQL
```

Cross-cutting concerns

- Security
- Validation
- Exception Handling
- Logging
- Caching

---

# Application Layers

## Controller Layer

Responsibilities

- REST Endpoints
- Request Validation
- Authentication
- Authorization
- Response Mapping

Controllers should not contain business logic.

---

## Service Layer

Responsibilities

- Business Rules
- Transactions
- Validation
- Cache Management
- Coordination between repositories

---

## Repository Layer

Responsibilities

- CRUD Operations
- Database Queries
- Entity Persistence

Implemented using Spring Data JPA.

---

## Database Layer

Responsibilities

- Persistent Storage
- Transactions
- Constraints
- Indexes
- Referential Integrity

---

# Component Responsibilities

| Component | Responsibility |
|-----------|----------------|
| Controller | HTTP Request Processing |
| Service | Business Logic |
| Repository | Data Access |
| Entity | Database Mapping |
| DTO | API Request/Response |
| Mapper | Entity ↔ DTO Conversion |
| Security Filter | JWT Validation |
| Exception Handler | Error Handling |
| Cache Manager | Redis Cache |

---

# Request Lifecycle

```text
Client
   |
HTTP Request
   |
   ▼
Security Filter
   |
JWT Validation
   |
Controller
   |
Request Validation
   |
Service
   |
Redis Lookup
   |
Cache Hit
   |
Return Response

Cache Miss
   |
Repository
   |
PostgreSQL
   |
Repository
   |
Store Result in Redis
   |
Service
   |
Controller
   |
HTTP Response
```

---

# Authentication Flow

```text
Client
   |
Login Request
   |
Authentication Service
   |
Validate Password
   |
Generate JWT
   |
Return JWT
   |
Client
   |
Authorization: Bearer <JWT>
   |
Security Filter
   |
Protected APIs
```

---

# Caching Architecture

Redis is used as a distributed cache.

## Cached Resources

- Course Details
- Module List
- Lesson List
- Course Metadata

Frequently changing entities such as

- Enrollment
- Progress

are retrieved directly from PostgreSQL.

---

## Cache Strategy

Read

```text
Read Through Cache
```

Write

```text
Cache Eviction
```

When Course, Module or Lesson changes

```
Database Updated

↓

Corresponding Cache Evicted

↓

Next Read Reloads Cache
```

---

## Benefits

- Faster API response
- Reduced database load
- Shared cache
- Better scalability

---

# Database Architecture

PostgreSQL is used as the primary transactional database.

Reasons

- ACID compliance
- Foreign Key constraints
- JSONB support
- Excellent indexing
- Mature ecosystem

Refer

- database.md
- schema.md

---

# Deployment Architecture

The application is packaged as a Docker image.

Typical deployment

```text
                Docker Image
                     |
                     ▼
             Docker Container
                     |
       +-------------+--------------+
       |                            |
       ▼                            ▼
   PostgreSQL                  Redis
```

The application can be deployed

- Local Machine
- Docker Compose
- Linux VM
- Cloud VM
- Container Platforms

No code changes are required across environments.

---

# Containerization

Each Docker container contains

- Spring Boot Application
- JRE
- Required Libraries

Externalized configuration

- Database URL
- Database Credentials
- Redis URL
- JWT Secret
- Server Port

Environment specific configuration is injected using environment variables.

Example

```text
SPRING_DATASOURCE_URL

SPRING_DATASOURCE_USERNAME

SPRING_DATASOURCE_PASSWORD

SPRING_DATA_REDIS_HOST

JWT_SECRET
```

---

# Scalability

The application is stateless.

Multiple containers can run simultaneously behind a load balancer.

```text
               Load Balancer
                      |
        +-------------+-------------+
        |                           |
        ▼                           ▼
   LMS Container              LMS Container
        |                           |
        +-------------+-------------+
                      |
               Redis / PostgreSQL
```

Future improvements

- Horizontal Scaling
- Read Replicas
- Redis Cluster
- CDN
- Kafka Integration

---

# Reliability

Recommended production practices

- Health Check Endpoint
- Graceful Shutdown
- Automatic Restart
- Connection Pooling
- Database Backup
- Retry for transient failures

---

# Logging

Structured logging should include

- Timestamp
- Request ID
- User ID
- Endpoint
- HTTP Method
- Response Status
- Execution Time

Sensitive information such as passwords and JWT tokens must never be logged.

---

# Monitoring

Recommended production monitoring

Application Metrics

- JVM Memory
- Heap Usage
- CPU Usage
- Thread Count
- HTTP Requests
- API Latency

Infrastructure Metrics

- Container CPU
- Container Memory
- PostgreSQL Connections
- Redis Memory Usage

Future tools

- Prometheus
- Grafana
- Loki

---

# Design Principles

The application follows

- SOLID Principles
- Separation of Concerns
- Dependency Injection
- Stateless Architecture
- RESTful API Design
- DRY
- KISS
- Composition over Inheritance

---

# Future Enhancements

Potential future improvements

- Refresh Token Support
- Email Notifications
- File Upload using Object Storage
- Kafka Integration
- Distributed Tracing
- Rate Limiting
- Multi-tenancy
- Container Orchestration using Kubernetes
- CI/CD Pipeline

---

# References

| Document | Purpose |
|----------|---------|
| requirements.md | Functional and Non-Functional Requirements |
| api.md | API Standards |
| database.md | Logical Database Design |
| schema.md | Physical Database Schema |
| security.md | Security Architecture |
| deployment.md | Deployment Guide |
| openapi/openapi.yaml | API Contract |