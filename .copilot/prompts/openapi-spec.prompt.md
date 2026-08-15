# Generate OpenAPI Specification

Use the project context files as the source of truth for all API design standards, conventions, reusable schemas, naming rules, error handling, security, pagination, and coding guidelines.

Do **not** redefine or duplicate standards already present in the context. Follow them consistently.

## Task

Based on the feature requirements provided, generate a complete **OpenAPI 3.1** specification.

The generated specification must be production-ready and compatible with **OpenAPI Generator** for a Spring Boot 3 application.

## Requirements

* Follow all conventions defined in the referenced context files.
* Reuse existing schemas and common components whenever possible.
* Do not introduce duplicate models if an equivalent schema already exists.
* Generate well-documented endpoints with:
    * operationId
    * summary
    * description
    * title
    * tags
    * request examples
    * response examples
* Include appropriate validation constraints.
* Include standard success and error responses.
* Apply the project's authentication and authorization standards.
* Ensure the specification is backward compatible unless explicitly stated otherwise.
* update *application.yaml* for all environment to generate with appropriate config for swagger 

## Spring Boot Code Generation

The generated specification must support automatic generation of:

* Spring Boot Controller Interfaces
* Request DTOs
* Response DTOs
* Enum classes
* Bean Validation annotations
* Swagger/OpenAPI annotations

## Build Configuration

If the required dependencies or plugins are missing from the project, add:

1. Required Maven dependencies
2. OpenAPI Generator Maven Plugin configuration
3. if required, add maven-antrun-plugin as dependency to post process the generated code to convert `javax` to `jakarta` imports

Configure the generator to:

* Generate interfaces only
* Generate DTO models
* Use Spring Boot 3
* Use Jakarta EE
* Enable Bean Validation
* Generate API interfaces without implementations
* Produce code that compiles without manual changes

Do not generate these configurations if they already exist in the project.

## Output

Provide:

1. `openapi.yaml`
2. Any newly required Maven dependencies (only if missing)
3. OpenAPI Generator plugin configuration (only if missing)
4. Expected generated Java classes
5. Any assumptions made

