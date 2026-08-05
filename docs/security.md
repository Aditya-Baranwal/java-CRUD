# Security Design

This document describes the security architecture adopted by the Learning Management System (LMS).

---

# Table of Contents

- Security Principles
- Authentication
- Authorization
- User Roles
- Password Security
- JWT Token
- API Security
- Transport Security
- Secret Management
- Data Security
- Input Validation
- Protection Against Common Attacks
- Audit Logging
- Security Headers
- Security Best Practices

---

# Security Principles

The application follows these security principles:

- Authentication before authorization
- Least privilege access
- Defense in depth
- Fail secure
- Secure by default
- Zero trust between clients and services

---

# Authentication

The application uses **JWT (JSON Web Token)** based authentication.

Authentication flow:

```text
                +---------+
                |  Client |
                +---------+
                     |
              Login Request
                     |
                     v
             +----------------+
             | Authentication |
             |    Service     |
             +----------------+
                     |
             Validate Credentials
                     |
                     v
               Generate JWT
                     |
                     |
              Access Token
                     |
                     v
                Client Stores
                     |
                     |
      Authorization: Bearer <JWT>
```

---

# Login Flow

1. User submits email and password.
2. Password is verified against stored password hash.
3. JWT Access Token is generated.
4. Token is returned to the client.
5. Client includes the token in every API request.

Example

```
Authorization: Bearer eyJhbGc...
```

---

# Authorization

Role Based Access Control (RBAC) is used.

Each request is authenticated before authorization.

---

# User Roles

| Role | Description |
|------|-------------|
| ADMIN | Full system access |
| INSTRUCTOR | Manage own courses, modules and lessons |
| USER | Consume courses and update own progress |

---

# Permission Matrix

| Resource | ADMIN | INSTRUCTOR | USER |
|----------|:-----:|:----------:|:----:|
| Courses | CRUD | CRUD (Own) | Read |
| Modules | CRUD | CRUD (Own) | Read |
| Lessons | CRUD | CRUD (Own) | Read |
| Enrollments | Read | Read | Create, Read Own |
| Progress | Read | Read | Update Own |

---

# Password Security

Passwords are never stored in plain text.

Passwords are stored using

```
BCrypt
```

Example

```
$2a$10$...
```

Security measures

- Salt generated automatically
- Adaptive hashing algorithm
- One-way hashing
- Password never returned in APIs

---

# JWT Token

## Claims

Example payload

```json
{
    "sub": "25",
    "email": "user@example.com",
    "role": "USER",
    "iat": 1722600000,
    "exp": 1722603600
}
```

---

## Token Expiration

| Token | Expiry |
|--------|---------|
| Access Token | 1 Hour |
| Refresh Token| 30 Days |

---

## Validation

Every request validates

- Signature
- Expiration
- Issuer
- Subject
- Role

Invalid tokens return

```
401 Unauthorized
```

---

# API Security

All APIs except authentication endpoints require authentication.

Protected endpoints

```
/courses
/modules
/lessons
/enrollments
/progress
```

Public endpoints

```
/login
/register
```

---

# Transport Security

All communication occurs over

```
HTTPS
```

Benefits

- Encryption
- Integrity
- Authentication

TLS version

```
TLS 1.2+
```

---

# Secret Management

Sensitive configuration is never committed to source control. 
They would be stored in environment variables or secret management systems.

Secrets include

- JWT Secret
- Database Password
- Database Username
- API Keys
- SMTP Password

Development

```
application-dev.yml
```

Production

```
application-prod.yml
```

Default

```
application.yml
```

---

# Data Security

Sensitive data

- Password
- JWT Secret
- Database Credentials

Non-sensitive data

- Course Title
- Lesson Content
- Tags

Passwords are hashed before persistence.

---

# Input Validation

Every API validates

- Required fields
- Length
- Data type
- Email format
- Enum values
- Business rules

Example

```
Course title

Required

Maximum 100 characters
```

---

# SQL Injection Protection

The application uses

- JPA/Hibernate
- Prepared Statements
- Parameterized Queries

Raw SQL is avoided whenever possible.

---

# Cross Site Scripting (XSS)

Protection

- Request validation
- Output encoding
- Content Security Policy

---

# Cross Site Request Forgery (CSRF)

Since authentication uses JWT Authorization headers instead of cookies

```
CSRF Protection Disabled
```

---

# Rate Limiting

Recommended

| Endpoint | Limit |
|----------|-------|
| Login | 5 requests/minute |
| Register | 3 requests/minute |
| APIs | 100 requests/minute |

Possible implementations

- API Gateway
- NGINX
- Spring Cloud Gateway

---

# CORS

Allowed Origins

```
https://lms.example.com
```

Allowed Methods

```
GET
POST
PUT
DELETE
```

Allowed Headers

```
Authorization

Content-Type
```

---

# Audit Logging

The following events are logged

- Login
- Logout
- Course Creation
- Course Update
- Enrollment
- Lesson Completion
- Authorization Failure

Sensitive information is never logged.

Passwords and JWT secrets are excluded from logs.

---

# Security Headers

Recommended headers

```
Content-Security-Policy

Strict-Transport-Security

X-Content-Type-Options

X-Frame-Options

Referrer-Policy
```

---

# Session Management

The application is stateless.

No server-side HTTP session is maintained.

Authentication relies entirely on JWT.

---

# Error Handling

Authentication failure

```
401 Unauthorized
```

Authorization failure

```
403 Forbidden
```

Error responses do not expose in production. 
In default, local, and dev profiles, error responses may include stack traces for debugging.

- Stack traces
- Database errors
- Internal implementation details

---

# Logging Guidelines

Log

- Request ID
- User ID
- API
- Response Status
- Execution Time

Do not log

- Password
- JWT Token
- Database Password
- API Keys

---

# Security Best Practices

- HTTPS everywhere
- JWT authentication
- BCrypt password hashing
- Role Based Access Control (RBAC)
- Parameterized database queries
- Input validation
- Least privilege access
- Secure secret management
- Centralized authentication
- Audit logging
- Standard security headers
- Stateless services
- No sensitive information in logs
- Regular dependency updates