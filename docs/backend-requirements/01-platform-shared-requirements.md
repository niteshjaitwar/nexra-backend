# Platform And Shared Backend Requirements

Scope: common backend foundation used by every module.

## Current State

The backend already has shared API response objects, exception types, OpenAPI configuration, correlation ID handling, metrics logging, rate limiting, auditable persistence base, Flyway migrations, actuator, and CI verification.

## Bugs

- `BUG`: Remove or correct stale comments that still describe in-memory/reference behavior after persistence was added.
- `BUG`: Resolve recurring compiler warnings for serializable exception classes.
- `BUG`: Align docs that say some modules are pending when the codebase now contains module implementations.
- `BUG`: Ensure all error responses use one canonical API error contract across every module.

## Enhancements

- `ENHANCEMENT`: Consolidate repeated module filters for JWT parsing, JSON `401`/`403`, and request correlation into shared infrastructure.
- `ENHANCEMENT`: Add a shared tenant context resolver used by controllers and services.
- `ENHANCEMENT`: Add idempotency key support for create, approve, pay, import, webhook, and mutation endpoints.
- `ENHANCEMENT`: Add consistent pagination, sorting, filtering, and search request models.
- `ENHANCEMENT`: Add optimistic-lock conflict handling with clear `409` responses.
- `ENHANCEMENT`: Add structured logging policy and remove sensitive values from logs.

## Features

- `FEATURE`: Audit service with append-only business events.
- `FEATURE`: Notification service for email, SMS, WhatsApp, push, and in-app events.
- `FEATURE`: File/document service with storage provider abstraction, virus scanning hook, metadata, retention, and signed URLs.
- `FEATURE`: Workflow/approval engine with policy rules, stages, escalation, delegation, and SLA timers.
- `FEATURE`: Reporting read-model service for dashboards and exports.
- `FEATURE`: Search indexing service for employees, CRM records, tickets, documents, and audit logs.
- `FEATURE`: Import/export framework with async jobs, row-level validation, templates, and error reports.
- `FEATURE`: Background job framework for retries, scheduled tasks, payroll generation, reminders, and cleanup.
- `FEATURE`: Webhook and integration event framework with outbox pattern and delivery retries.
- `FEATURE`: Feature flag and tenant configuration service.

## Security Requirements

- `SECURITY`: Map all public APIs to OWASP API Security Top 10 controls.
- `SECURITY`: Define an ASVS target level. Use L2 for normal business workflows and L3 for auth, payroll, payments, audit, and admin actions.
- `SECURITY`: Add object-level authorization tests for every endpoint that reads or mutates tenant-owned data.
- `SECURITY`: Add function-level authorization tests for admin, HR admin, payroll admin, manager, employee, sales, support, and platform roles.
- `SECURITY`: Add abuse-path rate limits for login, OTP, refresh, imports, exports, reports, document downloads, webhooks, and mutation endpoints.
- `SECURITY`: Add secret scanning in CI.
- `SECURITY`: Add dependency vulnerability scanning in CI.
- `SECURITY`: Add SAST and API DAST gates before release.
- `SECURITY`: Add data classification: public, internal, confidential, payroll, identity, compliance.
- `SECURITY`: Add encryption-at-rest expectations for sensitive columns and storage objects.
- `SECURITY`: Add field-level masking for PII/payroll responses where role requires it.

## Data Requirements

- `DATA`: Define tenant deletion, suspension, export, and retention lifecycle.
- `DATA`: Add migration tests against clean database and previous release database.
- `DATA`: Add seed-free production startup validation.
- `DATA`: Define archival strategy for audit logs, payroll slips, documents, and high-volume operational records.

## Operability Requirements

- `OPERABILITY`: Add Dockerfile and production container hardening.
- `OPERABILITY`: Add deployment manifests for the chosen runtime.
- `OPERABILITY`: Add environment variable inventory.
- `OPERABILITY`: Add health, readiness, dependency, and business-process checks.
- `OPERABILITY`: Add dashboards for API latency, error rate, DB pool, JVM, queue/job status, login failures, payroll runs, notification failures, and webhook failures.
- `OPERABILITY`: Add alert rules and severity mapping.
- `OPERABILITY`: Add backup, restore, rollback, and incident runbooks.

## Tests

- `TEST`: Add module boundary tests.
- `TEST`: Add authorization matrix tests.
- `TEST`: Add migration tests.
- `TEST`: Add API contract tests.
- `TEST`: Add performance tests for list/search/export endpoints.
- `TEST`: Add resilience tests for Redis/mail/storage/database dependency failures.

## Done Criteria

- No placeholder workflow endpoints remain in platform-facing APIs.
- Shared services are reusable by all modules.
- Security, audit, notification, file, workflow, and reporting primitives are production usable.
- Production can be deployed, monitored, backed up, restored, and rolled back with documented steps.

