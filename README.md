# Nexra Backend

Spring Boot backend for **Nexra HRMS and CRM**, organized as a modular monolith with clear module boundaries and shared platform conventions.

## Overview

Nexra Backend runs as a single Spring Boot application. Each domain module (auth, HRMS, payroll, CRM, operations, workflow) keeps its own controllers, services, and persistence while sharing common API contracts, security, logging, and database migration practices.

- **Architecture:** modular monolith
- **Domains:** HRMS, CRM, payroll, operations, workflow
- **Database:** MySQL at runtime, H2 in tests
- **Schema:** Flyway migrations (currently v62)

## Modules

| Module | Scope |
| --- | --- |
| `auth` | Registration, login, MFA, sessions, OAuth2, tenants |
| `hrms.employee` | Organization profile, departments, employees |
| `hrms.attendance` | Shifts, punch records, regularization |
| `hrms.leave` | Leave types, balances, requests |
| `hrms.timesheet` | Projects, time entries |
| `hrms.onboarding` | Onboarding workflows |
| `hrms.performance` | Performance reviews |
| `hrms.recruitment` | Job postings and candidates |
| `hrms.expense` | Expense claims |
| `payroll` | Payslips, statutory packs, filing export (JSON/XML) |
| `crm` | Leads, accounts, contacts, deals, campaigns, quotes, cases, webhooks |
| `operations` | Projects, tasks, approvals |
| `workflow` | Multi-step workflow runtime with SLA support |

## Platform conventions

- Canonical `ApiResponse` wrapper for REST endpoints
- Centralized exception handling and request correlation (`X-Request-Id`)
- Security headers (HSTS, CSP, frame options, referrer policy)
- Rate limiting with optional Redis-backed distributed mode
- Optimistic locking via `@Version` on shared persistence base entities
- OpenAPI documentation and Actuator health/metrics endpoints
- CRM webhook HMAC verification with timestamp skew and replay protection
- CI: Maven verify, JaCoCo coverage gate, OWASP dependency-check, CodeQL, Trivy

## Tech stack

- Java 25
- Spring Boot 4
- Spring Security + OAuth2 Authorization Server
- Spring Data JPA + Flyway
- Redis (auth throttling and rate limiting)
- Micrometer + Prometheus
- OpenTelemetry tracing
- Thymeleaf + OpenHTMLToPDF + PDFBox (payslip PDF generation)

## Project layout

```text
src/main/java/com/nexra/hrms/nexra/
  common/          # shared API, security, logging, rate limiting, workflow
  modules/
    auth/
    crm/
    hrms/          # attendance, employee, expense, leave, onboarding, ...
    operations/
    payroll/
```

## Run locally

### Linux / macOS

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Windows (PowerShell)

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

The `dev` profile uses local defaults for the database and auth secrets. Override with `AUTH_DB_*`, `AUTH_JWT_SECRET`, and `AUTH_OAUTH2_DEFAULT_CLIENT_SECRET` when your local MySQL setup differs.

## Profiles

Environment-specific property sets exist for `dev`, `test`, `e2e`, `stage`, and `prod`, with module overlays for auth, CRM, HRMS, payroll, expense, and workflow.

## Build and test

```bash
./mvnw verify
```

This runs compilation, unit and integration tests, JaCoCo coverage checks, and Maven enforcer rules.

## API routes

| Prefix | Domain |
| --- | --- |
| `/api/v1/auth/*` | Authentication and sessions |
| `/api/v1/employee-core/*` | Employee and organization |
| `/api/v1/attendance/*` | Attendance |
| `/api/v1/leave/*` | Leave management |
| `/api/v1/timesheet/*` | Timesheets |
| `/api/v1/payroll/*` | Payroll and statutory filings |
| `/api/v1/crm/*` | CRM |
| `/api/v1/operations/*` | Operations |
| `/api/v1/workflows/*` | Workflow engine |

## Documentation

- `instruction.md` — project conventions and contributor guidance
- `PRODUCTION_READINESS.md` — code-level readiness notes and deployment checklist

## License

Proprietary — Nexra internal platform.
