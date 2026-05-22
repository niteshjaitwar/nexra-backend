# Nexra Release Evidence Template

Use this file as the mandatory evidence pack for every release candidate.

Automation helper:

- Windows PowerShell: `.\build\release\Generate-ReleaseEvidence.ps1 -ReleaseVersion <version> -Environment <env> -Approver <name>`
- Performance/Resilience helper: `.\build\release\Generate-PerfResilienceEvidence.ps1 -Environment <env> -LoadResult <PASS|FAIL> -SoakResult <PASS|FAIL> -DependencyFailureResult <PASS|FAIL> -DbPoolExhaustionResult <PASS|FAIL> -EvidenceLinks "<links>"`
- Reproducible image helper: `.\build\release\Build-ReproducibleImage.ps1 -ImageName <registry/name> -ImageTag <version>`
- Output file: `build/release/release-evidence.generated.md`

Release metadata:

- Release version:
- Commit SHA:
- Build date (UTC):
- Environment:
- Approver:

## 1) Build And Test Evidence

- Command: `.\mvnw.cmd --batch-mode verify`
- Result:
- Tests total:
- Failures:
- Errors:
- Skipped:
- Coverage line ratio:
- Report links:

## 2) Security Gate Evidence

- SAST result:
- Dependency scan result:
- Secret scan result:
- API authz negative suite result:
- Tenant isolation suite result:
- Privilege escalation suite result:
- Evidence links:

## 3) Data And Migration Evidence

- Clean database migration result:
- Previous-release migration result:
- Rollback/forward-fix note link:
- Backup test result:
- Restore test result:
- Evidence links:

## 4) Product Workflow E2E Evidence

- Auth provisioning + login/refresh/logout:
- Employee lifecycle:
- Attendance to payroll input:
- Leave to payroll input:
- Timesheet approval:
- Recruitment to onboarding to employee:
- Expense to reimbursement/payroll:
- Payroll generation and payslip release:
- CRM lead baseline workflow:
- Evidence links:

## 5) Observability Evidence

- Logs with request ID/tenant/actor:
- Metrics dashboard link:
- Alerts dashboard link:
- Tracing dashboard link:
- On-call route check:
- Runbook link: `docs/runbooks/observability-oncall-handoff-runbook.md`

## 6) Performance And Resilience Evidence

- Load test result:
- Soak test result:
- Dependency failure test result:
- DB connection exhaustion test result:
- Evidence links:
- Runbook link: `docs/runbooks/performance-resilience-validation-runbook.md`

## 7) Release Operations Evidence

- Reproducible image build result:
- Deployment manifest reference:
- Environment variable inventory link:
- Release checklist link:
- Rollback checklist link:
- Incident runbook link:
- Deployment runbook link: `docs/runbooks/deployment-manifest-runbook.md`

## Sign-off

- Engineering sign-off:
- QA sign-off:
- Security sign-off:
- Product sign-off:
