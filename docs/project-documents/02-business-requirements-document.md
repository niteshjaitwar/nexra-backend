# Business Requirements Document

## Purpose

This BRD defines the business needs, objectives, stakeholders, scope, constraints, and launch success criteria for the Nexra platform.

## Business Problem

Small and mid-market businesses often manage HR, payroll, sales, expenses, and operational approvals through separate tools or spreadsheets. This creates duplicated data, approval delays, payroll errors, weak audit trails, and poor management visibility.

Nexra must solve this by providing one secure SaaS platform with modular workflows for HRMS, payroll, CRM, and operations.

## Business Objectives

- Reduce manual HR and payroll administration.
- Provide tenant-safe employee, attendance, leave, payroll, expense, recruitment, performance, and CRM workflows.
- Support phased customer onboarding by product module.
- Build a foundation for enterprise-grade compliance, audit, reporting, and integrations.
- Reach a market-launch backend that can support commercial pilots.

## Stakeholders

- Executive sponsor: owns commercial goals and pricing decisions.
- Product owner: owns roadmap, feature priority, and release acceptance.
- Engineering lead: owns architecture, code quality, security, and delivery.
- QA lead: owns test strategy, regression readiness, and release confidence.
- DevOps lead: owns environments, deployment, observability, and rollback.
- Customer success: owns onboarding playbooks, training feedback, and support readiness.
- Tenant admin: manages company setup, products, users, departments, and approvals.
- End users: employees, managers, HR, payroll, finance, and sales users.

## In Scope

- Multi-tenant identity, registration, login, verification, refresh tokens, and product access.
- Employee core: organization profile, departments, employee records.
- Attendance: shifts, check-in, check-out, records, summary.
- Leave: leave types, holidays, balances, requests, approvals.
- Timesheet: projects, entries, approval, summary.
- Onboarding: plans, tasks, completion, summary.
- Performance: goals, reviews, summary.
- Recruitment: jobs, candidates, stage history, summary.
- Expense: categories, claims, approvals, reimbursements.
- Payroll: organization payroll profile, employee payroll profile, payroll generation, payslip HTML/PDF.
- CRM baseline: leads and schema foundation for accounts, contacts, deals, activities, and tasks.
- Audit, metrics, security headers, rate limiting, OpenAPI, Flyway migrations, and CI quality gates.

## Out Of Scope For MVP

- Full statutory payroll engine for every country.
- Native mobile apps.
- AI automation.
- Advanced CRM marketing automation.
- Full operations suite such as inventory, procurement, assets, and contracts.
- Marketplace integrations.
- Advanced analytics warehouse.

## Business Requirements

| ID | Requirement | Priority |
| --- | --- | --- |
| BR-001 | Tenant admins can provision and configure a company tenant. | Must |
| BR-002 | Users can register, verify, authenticate, refresh sessions, and logout. | Must |
| BR-003 | Tenant admins can grant product access by module. | Must |
| BR-004 | HR can manage organization, departments, and employees. | Must |
| BR-005 | Employees and managers can manage attendance, leave, timesheets, expenses, onboarding, and performance workflows. | Must |
| BR-006 | Payroll team can configure payroll profiles, generate payroll slips, and retrieve payslips. | Must |
| BR-007 | Sales users can manage CRM leads with tenant isolation. | Must |
| BR-008 | All sensitive workflows must be auditable. | Must |
| BR-009 | Product modules can be launched incrementally without separate deployments per module. | Should |
| BR-010 | System can support future CRM, operations, documents, notifications, reporting, and workflow engine modules. | Should |

## Success Metrics

- Tenant onboarding completed in less than 1 business day for pilot customers.
- Core HRMS workflows completed without spreadsheet fallback.
- Payroll generation and payslip retrieval are repeatable and auditable.
- Critical API uptime target for launch: 99.5 percent monthly.
- Backend regression suite passes before every release.
- No known critical or high security findings at launch.

## Key Risks

- Payroll and statutory compliance can become geography-specific quickly.
- HR and payroll data requires strong privacy controls and audit evidence.
- Broad module scope can dilute MVP focus.
- CRM and operations depth must not block HRMS/payroll launch readiness.

## Assumptions

- Backend remains a modular monolith for the current product stage.
- MySQL is the production database.
- H2 is used for automated tests.
- Redis supports auth hardening flows.
- Frontend consumes REST APIs exposed by this backend.
- Production secrets are externalized and never committed.
