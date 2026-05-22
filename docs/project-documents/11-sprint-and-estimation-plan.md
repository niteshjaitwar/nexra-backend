# Sprint And Estimation Plan

## Delivery Model

Use two-week sprints with a release candidate at the end of every sprint. Keep one prioritized backlog across backend, frontend, QA, DevOps, and documentation.

## Estimation Scale

Use story points:

- 1: trivial change or documentation update
- 2: small endpoint, validation, or UI change
- 3: normal feature slice with tests
- 5: multi-layer workflow with persistence and authorization
- 8: complex module feature or cross-module integration
- 13: split required before sprint commitment

## Team Roles

- Product owner: backlog priority and acceptance.
- Tech lead: architecture, review standards, risk calls.
- Backend engineer: API, business logic, persistence, tests.
- Frontend engineer: UI workflows and API integration.
- QA engineer: test cases, regression, exploratory, automation.
- DevOps engineer: environments, deployments, monitoring.

## Capacity Assumptions

Baseline two-week sprint capacity:

| Role | Assumed Availability | Planning Capacity |
| --- | --- | --- |
| Product owner | 50 percent | backlog, acceptance, customer priority |
| Tech lead | 70 percent | architecture, review, risk removal |
| Backend engineer 1 | 80 percent | 18 to 22 points |
| Backend engineer 2 | 80 percent | 18 to 22 points |
| Frontend engineer | 70 percent | 12 to 18 points, starts after API contract stability |
| QA engineer | 70 percent | 12 to 18 points plus regression ownership |
| DevOps engineer | 40 percent | 6 to 10 points plus environment support |

Planning rule: sprint commitment should not exceed 80 percent of calculated capacity. Keep 20 percent for defects, review, release work, and production-readiness gaps.

## Sprint Dependency Map

| Sprint | Depends On | Blocks |
| --- | --- | --- |
| Sprint 0 | none | all delivery sprints |
| Sprint 1 | docs baseline, dev/test environments | all protected module workflows |
| Sprint 2 | Sprint 1 auth and tenant context | attendance, leave, payroll employee linkage |
| Sprint 3 | employee core and manager roles | payroll readiness, approval queues |
| Sprint 4 | employee, attendance, leave, expense basics | release candidate payroll workflow |
| Sprint 5 | employee and product access | HR dashboard and lifecycle workflows |
| Sprint 6 | CRM schema and product access | launch API freeze |
| Sprint 7 | all MVP modules | production deployment |

## Sprint 0: Foundation

Goals:

- finalize BRD, PRD, MVP, HLD, ERD, API, security, QA, DevOps docs
- confirm environments
- confirm CI pipeline
- confirm coding standards
- create release checklist

Exit criteria:

- documentation baseline approved
- build and tests run locally and in CI
- MVP backlog created

## Sprint 1: Auth And Tenant Readiness

Scope:

- tenant provisioning hardening
- product access edge cases
- verification flow tests
- authorization negative tests
- OpenAPI review

Estimate: 30 to 40 points

## Sprint 2: Employee Core And Attendance

Scope:

- employee profile completion
- department workflows
- attendance shift and record flows
- summary APIs
- tenant isolation tests

Estimate: 35 to 45 points

## Sprint 3: Leave And Timesheet

Scope:

- leave policy and balance flows
- leave approvals
- timesheet project and entry approvals
- approval audit events

Estimate: 35 to 50 points

## Sprint 4: Expense And Payroll

Scope:

- expense claim lifecycle
- reimbursement flow
- payroll profile setup
- payroll generation
- payslip HTML/PDF verification

Estimate: 45 to 60 points

## Sprint 5: Recruitment, Onboarding, Performance

Scope:

- onboarding plans and task completion
- goals and reviews
- jobs, candidates, stage history
- dashboard summaries

Estimate: 40 to 55 points

## Sprint 6: CRM Lead Baseline And Launch Hardening

Scope:

- lead lifecycle hardening
- remove or replace placeholder CRM mutation path
- production readiness checks
- deployment and observability run-through

Estimate: 35 to 50 points

## Sprint 7: Release Candidate

Scope:

- full regression
- security test pass
- load smoke test
- backup and restore drill
- release notes
- pilot tenant setup

Estimate: 25 to 40 points

## Definition Of Ready

- user story has business outcome
- acceptance criteria are testable
- API contract or UI expectation is clear
- data and security impact are known
- dependencies are identified

## Definition Of Done

- implementation complete
- tests added or updated
- tenant isolation verified
- security impact reviewed
- OpenAPI updated
- migration added if schema changed
- `.\mvnw.cmd --batch-mode verify` passes
- documentation updated when behavior changes

Quality gate:

```powershell
.\mvnw.cmd --batch-mode verify
```

## Estimation Risks

- payroll compliance work can exceed normal estimates
- cross-module workflows require extra integration testing
- frontend estimates depend on final API stability
- production hardening often exposes configuration and observability gaps
