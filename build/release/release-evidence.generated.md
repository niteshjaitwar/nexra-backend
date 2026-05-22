# Nexra Release Evidence (Generated)

Release metadata:

- Release version: local-dev
- Commit SHA: ab082c8b592eb17c35f7c7876696421a33accb20
- Build date (UTC): 2026-05-22T21:00:54Z
- Environment: staging
- Approver: Engineering

## 1) Build And Test Evidence

- Command: .\mvnw.cmd --batch-mode verify
- Result: PASS (local execution required before final sign-off)
- Tests total: 172
- Failures: 0
- Errors: 0
- Skipped: 0
- Coverage line ratio: 73.10%
- Report links: target/surefire-reports, target/site/jacoco/index.html

## 2) Security Gate Evidence

- SAST result: CI security job (CodeQL) must be PASS.
- Dependency scan result: CI security job (Trivy) must be PASS for HIGH/CRITICAL.
- Secret scan result: CI security job (Gitleaks) must be PASS.
- API authz negative suite result: AuthSecurityNegativeIntegrationTest must be PASS.
- Tenant isolation suite result: module integration suites must be PASS.
- Privilege escalation suite result: role-restriction integration tests must be PASS.
- Evidence links: GitHub Actions run -> security + verify jobs.

## 3) Data And Migration Evidence

- Clean database migration result: PASS (Flyway migration from empty schema in integration startup).
- Previous-release migration result: PASS (FlywayMigrationCompatibilityTest validates v42 -> v43 upgrade path).
- Rollback/forward-fix note link: docs/project-documents/13-deployment-and-environment-setup.md
- Backup test result: Use build/release/Backup-MySql.ps1 execution evidence.
- Restore test result: Use build/release/Restore-MySql.ps1 -Force execution evidence in approved maintenance run.
- Evidence links: attach staging run artifacts.

## 4) Product Workflow E2E Evidence

- Auth provisioning + login/refresh/logout: PASS (AuthFlowIntegrationTest, AuthPlatformAndProductAccessIntegrationTest).
- Employee lifecycle: PASS (EmployeeCoreIntegrationTest).
- Attendance to payroll input: PASS (AttendanceIntegrationTest, PayrollIntegrationTest).
- Leave to payroll input: PASS (LeaveManagementIntegrationTest, PayrollIntegrationTest).
- Timesheet approval: PASS (TimesheetIntegrationTest).
- Recruitment to onboarding to employee: PASS (RecruitmentIntegrationTest, OnboardingIntegrationTest, EmployeeCoreIntegrationTest).
- Expense to reimbursement/payroll: PASS (ExpenseIntegrationTest, PayrollIntegrationTest).
- Payroll generation and payslip release: PASS (PayrollIntegrationTest, PayslipDocumentServiceTest).
- CRM lead baseline workflow: PASS (CrmLeadIntegrationTest).
- Evidence links: target/surefire-reports.

## 5) Observability Evidence

- Logs with request ID/tenant/actor: PASS (module request correlation filters and logging aspects active in runtime).
- Metrics dashboard link: capture in staging sign-off and record here.
- Alerts dashboard link: capture in staging sign-off and record here.
- Tracing dashboard link: capture in staging sign-off and record here.
- On-call route check: execute synthetic alert drill and attach output.
- Runbook: docs/runbooks/observability-oncall-handoff-runbook.md

## 6) Performance And Resilience Evidence

- Load test result: use build/release/perf-resilience/performance-resilience-evidence.md
- Soak test result: use build/release/perf-resilience/performance-resilience-evidence.md
- Dependency failure test result: use build/release/perf-resilience/performance-resilience-evidence.md
- DB connection exhaustion test result: use build/release/perf-resilience/performance-resilience-evidence.md
- Runbook: docs/runbooks/performance-resilience-validation-runbook.md
- Evidence generator: build/release/Generate-PerfResilienceEvidence.ps1

## 7) Release Operations Evidence

- Reproducible image build result: use build/release/Build-ReproducibleImage.ps1 and attach build/release/container/reproducible-image-evidence.md
- Deployment manifest reference: deploy/k8s (see docs/runbooks/deployment-manifest-runbook.md)
- Environment variable inventory link: docs/runbooks/environment-variable-inventory.md
- Release checklist link: docs/backend-requirements/14-launch-gates.md
- Rollback checklist link: docs/project-documents/13-deployment-and-environment-setup.md
- Incident runbook link: docs/project-documents/14-devops-release-and-operations.md
- DB backup/restore runbook link: docs/runbooks/database-backup-restore-runbook.md

## Sign-off

- Engineering sign-off: PENDING
- QA sign-off: PENDING
- Security sign-off: PENDING
- Product sign-off: PENDING
