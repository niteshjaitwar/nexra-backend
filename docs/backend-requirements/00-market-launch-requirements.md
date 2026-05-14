# Nexra Backend Market Launch Requirements

Status: Draft baseline  
Scope: Backend only  
Review date: 2026-05-13  
Target: Enterprise-grade modular monolith for HRMS, CRM, payroll, and operations

## Purpose

This document converts the current backend state into an end-to-end requirement plan for reaching market launch quality. It is not a UI plan. Frontend work is intentionally out of scope until backend product contracts, security, integrations, data model, and operational readiness are complete.

## Current Backend Snapshot

The repository already has a working Spring Boot modular monolith with these implemented or partially implemented modules:

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

The current verification gate passes:

- Command: `.\mvnw.cmd --batch-mode verify`
- Result: `139 tests`, `0 failures`, `0 errors`, `0 skipped`
- Approximate line coverage: `73.05%`

This is a solid backend foundation, but it is not yet market-launch complete for a Zoho/ADP-class product. The main gap is product depth, workflow integration, compliance, and operational hardening.

## Requirement Classification

Use these labels in issues and planning:

- `BUG`: incorrect, misleading, unsafe, incomplete, or inconsistent behavior in existing code.
- `ENHANCEMENT`: improves existing capability without changing the business domain scope.
- `FEATURE`: new business capability required for product completeness.
- `INTEGRATION`: connects modules, external systems, notifications, files, search, reporting, or audit.
- `SECURITY`: confidentiality, integrity, availability, abuse prevention, tenant isolation, compliance, or secure SDLC work.
- `OPERABILITY`: deployment, monitoring, backup, disaster recovery, incident response, runbooks, or production support.
- `DATA`: schema, migration, retention, privacy, import/export, or data quality requirement.
- `TEST`: unit, integration, contract, E2E, performance, security, migration, or resilience verification.

## External Standards Baseline

As of May 2026, backend launch requirements should map to these active references:

- OWASP API Security Top 10 2023 for API-specific risks: https://owasp.org/API-Security/
- OWASP ASVS 5.0 as the application security verification baseline where applicable: https://github.com/OWASP/ASVS
- NIST SP 800-218 SSDF 1.1 for secure software development process: https://csrc.nist.gov/pubs/sp/800/218/final
- CIS Controls v8.1 for operational cybersecurity control coverage: https://www.cisecurity.org/controls/v8-1
- ISO/IEC 27001:2022 as a future organizational ISMS target, not a code-only checklist: https://www.iso.org/standard/27001

## Market Launch Definition

The backend is market-launch ready only when all of these are true:

- Every public API has authentication, authorization, tenant isolation, validation, pagination, rate limiting where needed, and OpenAPI documentation.
- Every business-critical action writes an audit event.
- Every module has durable persistence, Flyway migrations, rollback/migration notes, and integration tests.
- Every module exposes real workflows, not placeholder acceptance endpoints.
- Cross-module workflows are implemented through internal application services, not duplicated controller logic.
- Secrets, signing keys, OAuth keys, mail credentials, storage credentials, and database credentials are externally managed.
- Production deployment can be repeated from a clean environment.
- Backup and restore are tested.
- Logs, metrics, traces, alerts, and dashboards exist for business and technical failure modes.
- Security testing includes SAST, dependency scanning, secret scanning, DAST/API testing, authorization negative tests, and abuse-path tests.
- Load testing proves the first commercial target capacity.

## Priority Roadmap

### Phase 1: Stabilize Foundation

- Fix misleading comments and stale docs.
- Consolidate repeated module security, correlation, exception, and audit patterns.
- Add module boundary tests.
- Add missing serialVersionUID or suppress compiler warnings consistently.
- Standardize tenant normalization and authorization checks.
- Add architecture decision records for modular monolith boundaries.

### Phase 2: Complete Core Platform Services

- Audit trail module.
- Notification service.
- File/document service.
- Workflow/approval engine.
- Reporting/read-model foundation.
- Search foundation.
- Import/export framework.
- Background job scheduler.
- Idempotency and retry framework.
- Webhook/integration event framework.

### Phase 3: Complete HRMS And Payroll

- Deepen employee lifecycle, leave policy, attendance, shift, recruitment, onboarding, performance, timesheet, expense, and payroll workflows.
- Integrate payroll with attendance, leave, timesheet, expenses, and employee master data.
- Add statutory/compliance engines per launch geography.

### Phase 4: Complete CRM

- Add accounts, contacts, deals, activities, tasks, support tickets, campaigns, pipeline forecasting, and automation.
- Replace CRM placeholder mutation endpoints with real domain workflows.

### Phase 5: Add Operations Suite

- Add project/task operations, assets, procurement, vendors, inventory, contracts, support desk, work orders, and internal approvals.

### Phase 6: Production Launch Gate

- Complete deployment manifests.
- Complete infrastructure docs.
- Complete monitoring and alerting.
- Complete backup/restore test.
- Complete security test evidence.
- Complete load/resilience test evidence.
- Complete release, rollback, and incident runbooks.

## Documentation Map

- [01 Platform Shared Requirements](./01-platform-shared-requirements.md)
- [02 Auth Requirements](./02-auth-requirements.md)
- [03 Employee Core Requirements](./03-employee-core-requirements.md)
- [04 Attendance Requirements](./04-attendance-requirements.md)
- [05 Leave Requirements](./05-leave-requirements.md)
- [06 Timesheet Requirements](./06-timesheet-requirements.md)
- [07 Onboarding Requirements](./07-onboarding-requirements.md)
- [08 Performance Requirements](./08-performance-requirements.md)
- [09 Recruitment Requirements](./09-recruitment-requirements.md)
- [10 Expense Requirements](./10-expense-requirements.md)
- [11 Payroll Requirements](./11-payroll-requirements.md)
- [12 CRM Requirements](./12-crm-requirements.md)
- [13 Operations Requirements](./13-operations-requirements.md)
- [14 Launch Gates](./14-launch-gates.md)
