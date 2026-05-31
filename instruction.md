# Nexra Backend Engineering Instructions

Applicable project: `nexra-backend`  
Architecture: Spring Boot modular monolith  
Primary focus: backend-only production readiness for HRMS, CRM, payroll, and operations  
Author: @niteshjaitwar  
Version: 2.0.0  
Last updated: 2026-05-13

## 1. Product Direction

Nexra is being built as an enterprise SaaS backend that can eventually compete with products like Zoho and ADP. The current priority is backend correctness, security, reliability, integrations, and production readiness. Frontend work must remain out of scope until the backend contracts and launch requirements are stable.

The backend must be treated as a modular monolith, not a microservice system. Keep one deployable Spring Boot application, but enforce strong internal module boundaries.

Reference requirement docs:

## 2. Core Engineering Rules

Every change must follow these rules:

- Prefer the existing module structure and naming conventions.
- Keep business logic inside services, not controllers.
- Keep controllers thin: validation, authentication context, service call, response wrapping.
- Keep repositories focused on persistence only.
- Use Flyway for every schema change.
- Do not use `ddl-auto` as a migration strategy.
- Do not introduce microservice infrastructure unless explicitly required.
- Do not add frontend, UI, or view-layer work in this repository.
- Do not add placeholder endpoints for production-facing features.
- Do not mark a module production-ready until tests, security, tenant isolation, audit, and migration requirements are complete.

## 3. Java And Spring Standards

- Use Spring Boot 4.x compatible APIs.
- Do not use deprecated Spring Security patterns such as `WebSecurityConfigurerAdapter`.
- Use constructor injection.
- Use `@Transactional` at service boundaries where data mutation or consistency is required.
- Use Bean Validation annotations for request DTOs.
- Use immutable DTO records where practical.
- Use `BigDecimal` for money and percentage calculations.
- Use `Instant` for audit timestamps and `LocalDate` for business dates.
- Normalize tenant codes consistently before persistence and lookup.

## 4. Documentation Standards

Java files should have clear class-level Javadocs when the purpose is not obvious. Public service methods and public API-facing methods should have useful Javadocs when they define business behavior or a contract.

Javadocs must:

- Explain purpose clearly.
- Avoid marketing language.
- Avoid HTML tags.
- Include `@author niteshjaitwar` at class level only where the surrounding module already follows that convention.
- Not include method-level `@author`.

Do not add noisy comments for obvious code. Add comments only when they clarify business rules, security rules, concurrency, money calculation, or integration behavior.

## 5. Mapping Rules

Use the simplest reliable mapping style for the module:

- Use explicit mapping when it protects clarity, avoids accidental field exposure, or handles security-sensitive fields.
- Use ModelMapper only where the module already uses it and the mapping is safe.
- Never expose entities directly from controllers.
- Never map sensitive fields into responses unless the role is allowed to see them.
- For payroll, identity, employee PII, and financial data, prefer explicit mapping.

## 6. API Contract Rules

Every public endpoint must have:

- Stable path under the correct product/module namespace.
- Request validation.
- Tenant isolation.
- Authentication and authorization.
- Consistent response envelope.
- Consistent error format.
- Pagination for list endpoints.
- OpenAPI annotations where the module standard requires them.
- Tests for success, validation failure, authorization failure, and cross-tenant access.

Avoid generic mutation endpoints like `/records/mutate` for production features. Use explicit domain commands such as approve, reject, submit, assign, convert, close, lock, release, or reimburse.

## 7. Security Requirements

Security must be designed into every module. Follow current backend security expectations based on:

- OWASP API Security Top 10 2023
- OWASP ASVS 5.0 where applicable
- NIST SP 800-218 SSDF
- CIS Controls v8.1

Every module must enforce:

- Authentication for protected APIs.
- Tenant isolation on every read and write.
- Object-level authorization.
- Function-level authorization.
- Product entitlement checks where applicable.
- Rate limiting for abuse-prone endpoints.
- Sensitive data masking.
- Audit logging for high-value actions.
- No secrets, tokens, OTPs, passwords, hashes, bank data, payroll data, or private identifiers in logs.

High-sensitivity areas:

- Auth
- Payroll
- Employee personal data
- Bank/tax/statutory identifiers
- Audit logs
- Admin and product access

## 8. Modular Monolith Boundaries

Current module root:

`src/main/java/com/nexra/hrms/nexra/modules`

Expected modules:

- `auth`
- `hrms.employee`
- `hrms.attendance`
- `hrms.leave`
- `hrms.timesheet`
- `hrms.onboarding`
- `hrms.performance`
- `hrms.recruitment`
- `hrms.expense`
- `payroll`
- `crm`
- future `operations`
- future shared platform services

Rules:

- A module owns its domain model and tables.
- Cross-module calls should go through service contracts, not direct repository access, unless explicitly documented.
- Shared code belongs in `common` only when it is truly cross-cutting.
- Business logic must not be moved into `common`.
- New shared services must be reusable across modules: audit, notification, documents, workflow, search, reporting, jobs, integrations.

## 9. Persistence And Data Rules

- Use Flyway migrations for all database changes.
- Use clear table names and indexes for tenant-scoped queries.
- Add optimistic locking for mutable entities.
- Add created/updated audit fields using the existing shared persistence standard.
- Add unique constraints where business uniqueness exists.
- Add indexes for list, search, approval queue, and dashboard queries.
- Avoid hard deletes for business records that need audit or compliance history.
- Use immutable ledgers for payroll, leave balance, expense reimbursement, audit, and inventory-style movements.

Any schema change must include:

- Migration file.
- Entity update.
- Repository/service update.
- Integration test.
- Backward compatibility note when needed.

## 10. Logging, Metrics, And Audit

Use `@Slf4j` where logging is needed.

Logging rules:

- Log meaningful business action boundaries.
- Include request ID through MDC where available.
- Include tenant code and record IDs when safe.
- Do not log sensitive payloads.
- Avoid excessive entry/exit logs if metrics aspects already cover the method.

Metrics rules:

- Critical workflows must expose success/failure/latency metrics.
- Track business workflows such as payroll runs, approvals, imports, exports, notifications, webhooks, and auth failures.

Audit rules:

- Audit every high-value mutation.
- Audit sensitive reads for payroll, employee PII, documents, and admin functions.
- Audit events should be append-only.

## 11. Testing Requirements

Before a module is treated as complete, it needs:

- Unit tests for domain logic.
- Integration tests for APIs and persistence.
- Authorization negative tests.
- Tenant isolation tests.
- Validation tests.
- Migration tests where schema changes are made.
- E2E workflow tests for critical business flows.

Required command before handoff:

```powershell
.\mvnw.cmd --batch-mode verify
```

The build passing is required but not sufficient for production readiness. Launch readiness must also satisfy `docs/backend-requirements/14-launch-gates.md`.

## 12. Module Completion Checklist

A module is complete only when:

- It has real business workflows, not placeholder acceptance responses.
- Its data is durable and migration-controlled.
- It enforces tenant isolation.
- It enforces role and product authorization.
- It emits audit events for important actions.
- It integrates with required shared services.
- It has success, failure, validation, authz, and tenant tests.
- It has OpenAPI coverage.
- It is observable in logs, metrics, and health checks where applicable.
- It has no stale comments claiming in-memory/reference behavior.

## 13. Backend Launch Checklist

The backend is market-launch ready only when:

- All module requirement docs are implemented or explicitly deferred.
- Launch gates pass.
- Production config is seed-free and secret-safe.
- Docker/deployment manifests exist.
- Backup and restore are tested.
- Monitoring and alerting are configured.
- Load, resilience, and security tests pass.
- Release and rollback runbooks exist.

## 14. AI Agent Instructions

When using this file as guidance:

- Read the relevant requirement doc before implementing a module change.
- Prefer fixing existing module patterns over introducing unrelated abstractions.
- If a requirement conflicts with current code, make the smallest safe change and document the reason.
- Do not remove user changes.
- Do not silently weaken security to make tests pass.
- Do not create fake production readiness.
- Always report what was changed and what was verified.

