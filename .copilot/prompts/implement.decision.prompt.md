# Business Decision Implementation Guidelines

Implement the change described in the **updated business decision/document**.

## Rules

- Treat the updated decision as the **source of truth**.
- Read and follow all relevant `.md` files in the `context/` folder before implementation.
- Follow the coding standards defined in `context/coding-standards.md`.
- Use the `context/` documentation to understand existing business rules, architecture, and decisions.
- **Do not make assumptions.** If anything required is unclear, ambiguous, contradictory, or missing, **STOP and ask for clarification before changing code**.
- Inspect the existing codebase before implementation and understand the relevant architecture, patterns, APIs, data models, and tests.
- Implement the decision end-to-end across all affected layers.
- Update relevant OpenAPI/API specs, database/migrations, service/domain logic, controllers, and tests as required.
- Preserve existing behavior and decisions unless the updated decision explicitly changes them.
- Do not introduce unrelated refactoring, behavior, or speculative functionality.
- Follow existing project patterns and coding standards.
- Add or update tests for the new behavior and regression cases.
- Run relevant tests, validation, lint, type checks, and other project checks before completion.
- Never weaken, remove, or bypass existing tests to make the implementation pass.

## Clarification Gate

If implementation requires a business or technical decision that cannot be determined confidently from:

- The updated business decision
- Relevant files in `context/`
- The existing codebase

**STOP, explain the ambiguity, ask the specific question, and wait for clarification.**

Do not guess or silently make a decision.

## Completion

Briefly report:

- What changed
- Business rules implemented
- Files/layers affected
- Tests/checks run and results
- Any remaining concerns

> **Most important: Do not guess. Do not silently decide. Ask first.**
