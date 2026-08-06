# Security Development Skill

> **Skill Name**
>
> Production-Grade Application Security

---

# Purpose

This skill defines how an AI agent should design, implement, and review security for a Java Spring Boot microservice.

Security is **not a separate feature**.

Every generated component must be secure by default.

The AI should proactively identify vulnerabilities and recommend secure implementations.

---

# Security Principles

Always follow

- Zero Trust
- Least Privilege
- Defense in Depth
- Secure by Default
- Fail Securely
- Explicit Authorization
- Principle of Complete Mediation

Never trust

- Client input
- Headers
- Cookies
- JWT claims without verification
- External services

---

# Authentication

Support project authentication mechanism.

Possible implementations

- JWT
- OAuth2
- OpenID Connect
- Session Authentication
- API Key

Never implement custom authentication unless explicitly requested.

---

# Authorization

Authorization belongs in

- Spring Security
- Business Layer (ownership validation)

Examples

```
User owns booking

Instructor owns course

Admin role required
```

Never rely on UI restrictions.

Always validate permissions server-side.

---

# Input Validation

Validate every request.

Use

```
Bean Validation

@NotNull

@NotBlank

@Positive

@Email

@Pattern

@Size
```

Business validation belongs inside Service.

Never concatenate user input into SQL or commands.

---

# SQL Injection

Always use

- JPA
- Prepared Statements
- Parameterized Queries

Never generate

```java
String sql =
"SELECT * FROM user WHERE id = " + userId;
```

---

# Command Injection

Never pass user input directly into

- Shell
- Runtime.exec()
- ProcessBuilder

Always sanitize or avoid shell execution.

---

# Cross Site Scripting (XSS)

Escape user-generated output when rendered in HTML.

Validate HTML inputs.

Reject dangerous scripts unless explicitly allowed.

---

# Cross Site Request Forgery (CSRF)

For browser-based applications

Enable

Spring Security CSRF protection.

For stateless JWT APIs

Disable only when appropriate.

Never disable CSRF blindly.

---

# JWT Security

Always verify

- Signature
- Expiration
- Issuer
- Audience
- Algorithm

Never trust

JWT payload without verification.

Never store sensitive data inside JWT.

---

# Password Handling

Never store plain text passwords.

Always use

```
BCrypt
```

or project-approved password encoder.

Never log passwords.

Never expose passwords.

---

# Secret Management

Secrets must come from

- Environment Variables
- Secret Manager
- Vault
- Kubernetes Secrets

Never hardcode

```
Password

API Key

JWT Secret

Private Key
```

---

# Logging Security

Never log

- Password
- JWT
- API Key
- Credit Card
- CVV
- Aadhaar
- PAN
- Bank Account
- OAuth Token
- Refresh Token

Prefer

```
User ID

Request ID

Correlation ID
```

---

# File Upload

Always validate

- File Size
- MIME Type
- Extension
- Virus Scan (if supported)

Never trust

Uploaded filename.

Generate unique file names.

---

# API Security

Every endpoint should have

- Authentication
- Authorization
- Validation
- Proper status codes

Use

```
401 Unauthorized

403 Forbidden
```

appropriately.

---

# Rate Limiting

Protect public APIs.

Possible implementations

- Bucket4j
- API Gateway
- Reverse Proxy

Never assume clients behave correctly.

---

# Sensitive Data

Encrypt

- Passwords (hash)
- Personal Information (where required)
- Financial Information
- Tokens at rest if applicable

Mask sensitive information in responses.

---

# HTTP Headers

Recommend

```
Strict-Transport-Security

Content-Security-Policy

X-Content-Type-Options

Referrer-Policy

Permissions-Policy

Cache-Control
```

Do not remove security headers.

---

# HTTPS

All production communication must use

```
HTTPS
```

Never recommend HTTP for production.

---

# CORS

Configure explicitly.

Avoid

```
AllowedOrigins = *
```

for authenticated APIs.

Limit

- Origins
- Methods
- Headers

---

# Session Management

If using sessions

- Secure Cookie
- HttpOnly
- SameSite
- Timeout
- Session Rotation

Prevent session fixation.

---

# Error Messages

Never expose

- Stack trace
- SQL query
- Internal class names
- Database schema
- Secrets

Return generic messages to clients.

Log detailed errors internally.

---

# Dependency Security

Recommend

- Latest stable dependencies
- Security patch updates
- Dependency scanning

Flag

- Known vulnerable libraries
- Deprecated security APIs

---

# Data Access

Always enforce authorization before accessing data.

Never rely on

```
findById()
```

alone.

Example

```
findByIdAndOwnerId(...)
```

when ownership matters.

---

# Business Authorization

Examples

```
User can edit only own profile

Instructor can update only own course

Customer can cancel only own booking
```

Never skip ownership validation.

---

# External API Calls

When calling external systems

Apply

- HTTPS
- Timeout
- Retry
- Certificate validation

Never disable SSL verification.

---

# Serialization

Avoid exposing

- Entity objects
- Internal IDs
- Sensitive fields

Use Response DTOs.

---

# Database Security

Prefer

- Least privilege database user
- Read-only user for reporting
- Foreign Keys
- Constraints

Never use root credentials.

---

# Encryption

Use strong algorithms.

Recommended

```
AES-256

RSA-2048+

TLS 1.3
```

Avoid deprecated algorithms.

---

# Security Logging

Log

- Login success
- Login failure
- Authorization failure
- Admin operations
- Password change
- Account lock
- Security exceptions

Avoid excessive logging.

---

# Common Vulnerabilities

Always check for

- SQL Injection
- XSS
- CSRF
- SSRF
- XXE
- Open Redirect
- Path Traversal
- Broken Authentication
- Broken Authorization
- Sensitive Data Exposure
- Security Misconfiguration
- Insecure Deserialization

---

# AI Review Checklist

Before returning code verify

✓ Authentication enforced

✓ Authorization verified

✓ Input validated

✓ No SQL Injection

✓ No command injection

✓ Secrets externalized

✓ No sensitive logging

✓ Passwords hashed

✓ DTOs used

✓ HTTPS assumed

✓ Proper error handling

✓ Security headers considered

---

# Anti-Patterns

Never generate

- Hardcoded passwords
- Embedded API keys
- Disabled authentication
- Disabled authorization
- `permitAll()` for protected APIs
- Wildcard CORS for authenticated endpoints
- Plain text password storage
- String-concatenated SQL
- Exposed stack traces
- Logging JWTs
- Logging secrets
- Returning entity objects directly

---

# Definition of Done

Security implementation is complete only when

- Authentication implemented
- Authorization verified
- Validation complete
- Secrets externalized
- Sensitive logging avoided
- Secure error handling implemented
- OWASP Top 10 considered
- HTTPS assumed
- No known security anti-patterns
- Ready for production

---

# Example Invocations

### Secure an API

```
Secure CourseController
```

The AI should

- Add authentication
- Add authorization
- Validate requests
- Secure responses
- Follow Spring Security best practices

---

### Review Security

```
Review BookingService security
```

The AI should

- Identify vulnerabilities
- Check authorization
- Check data exposure
- Check injection risks
- Recommend improvements
- Preserve business behavior

---

### Generate Secure Authentication

```
Implement JWT authentication
```

The AI should generate

- Security configuration
- JWT validation
- Authentication filter
- Exception handling
- Password hashing
- Secure endpoint protection
- Production-ready implementation