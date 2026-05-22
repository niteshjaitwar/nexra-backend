# MVP Scope Document

## MVP Objective

Deliver a commercially usable backend for HRMS, payroll, and CRM lead management that supports pilot customers with secure multi-tenancy, auditable workflows, and repeatable deployment.

## MVP Theme

"Run core people operations and payroll with a CRM lead baseline from one secure tenant-scoped platform."

## MVP In Scope

### Platform

- Tenant provisioning
- User registration, verification, login, refresh, and logout
- Product access management
- OAuth client setup
- Rate limiting
- Request correlation
- Audit event foundation
- OpenAPI documentation
- Actuator health and Prometheus metrics

### HRMS

- Organization profile
- Departments
- Employees
- Attendance shifts and records
- Leave policies, balances, requests, approvals
- Timesheets and approvals
- Onboarding plans and tasks
- Goals and reviews
- Recruitment jobs, candidates, and stage history
- Expense claims, approvals, and reimbursement

### Payroll

- Organization payroll profile
- Employee payroll profile
- Payroll slip generation
- Payroll generation from employee profile
- Payslip HTML and PDF

### CRM

- Lead CRUD
- Lead list and status pipeline baseline
- Persistent CRM schema foundation for future accounts, contacts, deals, activities, and tasks

## MVP Out Of Scope

- Advanced statutory payroll by country and state
- Payroll banking integrations
- Native mobile app
- Full CRM opportunity forecasting
- Marketing campaigns
- Customer support desk
- Advanced workflow builder
- Document management beyond payslip rendering
- Data warehouse and BI layer
- Marketplace integrations
- Full operations modules

## MVP Acceptance Gates

| Gate | Required Result |
| --- | --- |
| Functional | Core workflows work through REST APIs for all MVP modules. |
| Security | Auth, authorization, tenant isolation, and product access are enforced. |
| Data | Flyway migrations create all MVP tables from clean database. |
| Audit | High-value mutations emit audit events or have a documented implementation gap. |
| Testing | Unit, integration, authorization, validation, and tenant tests pass. |
| Documentation | BRD, PRD, HLD, ERD, API, security, test, deployment, and operations docs exist. |
| DevOps | Repeatable deployment and rollback process is documented. |
| Observability | Health, metrics, request ID, and logs are available. |

## MVP Milestones

1. Stabilize platform and auth.
2. Complete employee core, attendance, leave, and timesheet integration.
3. Complete expense and payroll workflows.
4. Complete onboarding, performance, and recruitment workflow depth.
5. Complete CRM lead baseline and deprecate placeholder mutation behavior.
6. Complete production readiness, deployment, and operational runbooks.

## Post-MVP Roadmap

- CRM accounts, contacts, deals, activities, tasks, campaigns, and support.
- Notification service.
- Document and file storage service.
- Workflow approval engine.
- Reporting and analytics read models.
- Search foundation.
- Import and export framework.
- Operations modules: assets, procurement, inventory, contracts, vendors, work orders.
