# Product Requirements Document

## Product Summary

Nexra is a multi-tenant SaaS platform that combines HRMS, payroll, CRM, and operations into one modular product. The current backend exposes REST APIs for core HRMS, payroll, and CRM capabilities.

## Goals

- Provide reliable tenant-scoped business workflows.
- Give HR, payroll, finance, sales, managers, and employees a single source of truth.
- Keep the product modular so tenants can adopt HRMS, payroll, CRM, or future operations independently.
- Build a backend foundation that can support frontend, integrations, analytics, and mobile clients.

## Personas

- Platform admin: creates tenants and manages global settings.
- Tenant admin: manages company setup, modules, users, roles, and policies.
- HR admin: manages employee lifecycle and HR policies.
- Manager: approves leave, timesheets, expenses, performance reviews, and onboarding tasks.
- Employee: performs self-service tasks.
- Payroll admin: configures payroll and generates payslips.
- Finance admin: reviews expenses and reimbursements.
- Sales user: manages leads and pipeline records.

## Product Modules

### Identity And Access

Capabilities:

- tenant creation and provisioning
- user registration and login
- OTP and link verification
- refresh token lifecycle
- logout
- OAuth client administration
- product access grants

Acceptance criteria:

- protected APIs require valid authentication
- user access is constrained by tenant and product access
- token and verification flows are not logged with sensitive values

### Employee Core

Capabilities:

- organization profile
- departments
- employee records
- employee summary

Acceptance criteria:

- employee code and work email are unique per tenant
- all reads and writes are tenant-scoped
- employee records can link to user accounts

### Attendance

Capabilities:

- shift setup
- check-in and check-out
- attendance records
- summary reporting

Acceptance criteria:

- one attendance record per employee per work date
- check-out validates existing check-in state
- summaries support manager and HR views

### Leave

Capabilities:

- leave types
- holidays
- leave balances
- request creation
- approval and rejection

Acceptance criteria:

- balances are tenant and employee scoped
- approvals update request state consistently
- validation prevents invalid dates and duplicate policy codes

### Timesheet

Capabilities:

- project setup
- timesheet entry creation
- approval and rejection
- summary reporting

Acceptance criteria:

- entries are traceable by tenant, employee, project, and work date
- approval status drives payroll and reporting readiness

### Onboarding

Capabilities:

- onboarding plan creation
- task creation
- task completion
- plan and summary views

Acceptance criteria:

- tasks belong to a tenant-scoped onboarding plan
- completion updates task status and summary counts

### Performance

Capabilities:

- goals
- review cycles
- review completion
- performance summary

Acceptance criteria:

- one review per tenant, employee, and cycle
- manager and employee comments are retained

### Recruitment

Capabilities:

- jobs
- candidates
- candidate stage changes
- stage history
- recruitment summary

Acceptance criteria:

- candidates belong to tenant-scoped jobs
- stage history is append-only from the product perspective

### Expense

Capabilities:

- categories
- claims
- claim items
- approval, rejection, reimbursement

Acceptance criteria:

- claim totals match item totals
- reimbursement follows approval
- finance actions are auditable

### Payroll

Capabilities:

- payroll organization profile
- payroll employee profile
- payroll generation
- payroll generation from profile
- payslip HTML and PDF retrieval
- auth dependency check

Acceptance criteria:

- payroll is tenant-scoped
- money fields use decimal precision
- generated payslips are durable and retrievable

### CRM

Capabilities:

- lead create, read, list, update, delete
- pipeline summary baseline
- schema foundation for accounts, contacts, deals, activities, and tasks

Acceptance criteria:

- lead email uniqueness is enforced per tenant
- CRM records are tenant-scoped
- placeholder mutation endpoints are replaced before market launch

## Nonfunctional Requirements

- API response envelope is consistent.
- Public APIs are documented through OpenAPI.
- Database changes use Flyway.
- Sensitive data is not logged.
- All modules support request correlation.
- Critical workflows expose metrics and tests.
- Production startup validates critical secrets and configuration.

## Release Criteria

- MVP scope implemented and tested.
- `.\mvnw.cmd --batch-mode verify` passes.
- OpenAPI documentation is current.
- Production secrets are externally supplied.
- Backup, restore, rollback, and monitoring are tested.
