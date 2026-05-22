# Backend Launch Gates

Scope: final acceptance gates before commercial launch.

Evidence pack template: `docs/release-evidence-template.md`

## Gate 1: Architecture

- `TEST`: Module boundary tests pass.
- `TEST`: No accidental cross-module repository access except approved internal contracts.
- `OPERABILITY`: ADRs exist for modular monolith boundaries, shared services, data ownership, and integration events.
- `BUG`: No stale in-memory/reference placeholder comments remain.

## Gate 2: Security

- `SECURITY`: OWASP API Security Top 10 risk mapping completed.
- `SECURITY`: ASVS target level mapping completed.
- `SECURITY`: SAST, dependency scanning, and secret scanning pass.
- `SECURITY`: API DAST/authz negative tests pass.
- `SECURITY`: Tenant isolation tests pass for every module.
- `SECURITY`: Privilege escalation tests pass.
- `SECURITY`: Sensitive field masking tests pass.
- `SECURITY`: Production secrets are externalized and rotatable.

## Gate 3: Data And Migration

- `DATA`: Clean database migration test passes.
- `DATA`: Previous-release migration test passes.
- `DATA`: Rollback/forward-fix strategy documented.
- `DATA`: Backup and restore test passes.
- `DATA`: Retention and deletion policies documented.

## Gate 4: Product Workflows

- `TEST`: Tenant provisioning and product access E2E pass.
- `TEST`: User registration, verification, login, refresh, logout E2E pass.
- `TEST`: Employee lifecycle E2E pass.
- `TEST`: Attendance-to-payroll input E2E pass.
- `TEST`: Leave-to-payroll input E2E pass.
- `TEST`: Timesheet approval E2E pass.
- `TEST`: Recruitment-to-onboarding-to-employee E2E pass.
- `TEST`: Expense-to-reimbursement/payroll E2E pass.
- `TEST`: Payroll run approval and payslip release E2E pass.
- `TEST`: CRM lead-to-deal E2E pass.
- `TEST`: Operations ticket/project/asset workflow E2E pass after operations modules are built.

## Gate 5: Observability

- `OPERABILITY`: Logs include request ID, tenant, actor where safe, module, action, and error code.
- `OPERABILITY`: Metrics exist for API latency, error rate, auth failures, rate limits, DB pool, JVM, jobs, notifications, webhooks, payroll runs, imports, and exports.
- `OPERABILITY`: Dashboards exist for engineering and business operations.
- `OPERABILITY`: Alerts exist with severity, owner, and runbook link.
- `OPERABILITY`: Tracing exists for cross-module workflows.

## Gate 6: Performance And Resilience

- `TEST`: Load tests pass for target launch capacity.
- `TEST`: Soak tests pass for long-running stability.
- `TEST`: Large import/export tests pass.
- `TEST`: Report generation tests pass under load.
- `TEST`: Redis/mail/storage/external dependency failure tests pass.
- `TEST`: Database connection exhaustion and retry behavior are verified.

## Gate 7: Release Operations

- `OPERABILITY`: Docker image build is reproducible.
- `OPERABILITY`: Deployment manifests exist.
- `OPERABILITY`: Environment variable inventory exists.
- `OPERABILITY`: Release checklist exists.
- `OPERABILITY`: Rollback checklist exists.
- `OPERABILITY`: Incident response runbook exists.
- `OPERABILITY`: On-call dashboard and alert routing exist.

## Final Launch Rule

The backend cannot be called enterprise market-launch ready until every gate above is passed with evidence. Passing `mvnw verify` is necessary but not sufficient.
