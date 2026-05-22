# Nexra Production E2E Execution Plan

Scope: `auth`, `hrms.employee`, `hrms.attendance`, `hrms.leave`, `hrms.timesheet`, `hrms.onboarding`, `hrms.performance`, `hrms.recruitment`, `hrms.expense`, `payroll`, `crm`.

## Wave 1

Modules:

- auth
- hrms.employee
- hrms.attendance
- payroll

Required E2E outcomes:

- Tenant provisioning and product access.
- Register, verify, login, refresh, logout.
- Employee profile baseline and tenant isolation.
- Attendance to payroll input path.
- Payroll generation and payslip retrieval with authorization checks.

## Wave 2

Modules:

- hrms.leave
- hrms.timesheet
- hrms.expense

Required E2E outcomes:

- Leave request and approval flow with tenant and role boundaries.
- Timesheet submission and approval flow with tenant and role boundaries.
- Expense claim to approval/reimburse flow with guardrails.
- Leave/timesheet/expense to payroll integration checks where applicable.

## Wave 3

Modules:

- hrms.onboarding
- hrms.performance
- hrms.recruitment
- crm

Required E2E outcomes:

- Recruitment to onboarding to employee lifecycle baseline.
- Performance goals/reviews lifecycle with access checks.
- CRM lead lifecycle baseline with tenant and role boundaries.

## Release Gate Commands

- Build and tests: `.\mvnw.cmd --batch-mode verify`
- Evidence pack: fill `docs/release-evidence-template.md`

## Hard Blockers

- Any E2E failure in scoped modules.
- Any tenant isolation or authorization regression.
- Any launch-gate item in `docs/backend-requirements/14-launch-gates.md` without evidence.
