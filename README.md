# Nexra Backend

Production-grade Spring Boot modular monolith powering **Nexra HRMS + CRM**.

## Overview

Nexra Backend is a single Spring Boot runtime with strict internal module boundaries, shared cross-cutting standards, and CI-enforced quality gates.  
It is designed for fast feature delivery without microservice complexity while preserving enterprise reliability.

- **Architecture**: Modular monolith
- **Primary domain**: HRMS + CRM
- **Current status**: Production-ready service baseline with verified module hardening

## Implemented Modules

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
- `crm` (tenant-isolated leads, accounts, contacts, deals, activities, tasks, workflows, webhooks, and audit trails)

## Core Engineering Standards

- Shared API contract via canonical `ApiResponse`
- Shared exception model with centralized global handling
- Shared request correlation (`X-Request-Id`) and hardened security headers
- Shared global rate limiting with bounded in-memory key strategy
- Shared auditing and optimistic locking (`@Version`) via common persistence base
- Flyway-first schema evolution
- CRM webhook HMAC verification with timestamp skew checks and replay protection
- OpenAPI + actuator + Prometheus-ready metrics
- CI gates: Maven enforcer + JaCoCo coverage check

## Tech Stack

- Java 25
- Spring Boot 4
- Spring Security + OAuth2 Authorization Server
- Spring Data JPA + Flyway
- MySQL (runtime), H2 (tests)
- Redis (auth hardening flows)
- Micrometer + Prometheus registry
- OpenTelemetry tracing
- Thymeleaf + OpenHTMLToPDF + PDFBox (payslips)

## Project Structure

```text
src/main/java/com/nexra/hrms/nexra/
  common/
    api/
    exception/
    logging/
    openapi/
    persistence/
    ratelimit/
    web/
  modules/
    auth/
    crm/
    hrms/
      attendance/
      employee/
      expense/
      leave/
      onboarding/
      performance/
      recruitment/
      timesheet/
    payroll/
```

## Run Locally

### Linux / macOS

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Windows (PowerShell)

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

`dev` uses local workstation defaults for the auth datasource and auth secrets.
Override them with `AUTH_DB_*`, `AUTH_JWT_SECRET`, and
`AUTH_OAUTH2_DEFAULT_CLIENT_SECRET` when your local MySQL credentials differ.

## Runtime Profiles

The application includes environment-specific property sets for `dev`, `test`, `e2e`, `stage`, and `prod`, plus module-specific overlays for auth, CRM, HRMS, payroll, and expense domains.

## Verify Quality Gates

```bash
./mvnw verify
```

This executes:
- compile + unit/integration tests
- module production validator tests
- JaCoCo coverage check
- enforcer baseline rules

## API Surface

- `/api/v1/auth/*`
- `/api/v1/employee-core/*`
- `/api/v1/attendance/*`
- `/api/v1/leave/*`
- `/api/v1/timesheet/*`
- `/api/v1/payroll/*`
- `/api/v1/crm/*`

## Documentation

- `instruction.md` is the primary project instruction document.
- `README.md` is the primary project overview document.
- Other markdown documentation has been intentionally removed from this repository per current policy.

## License

Proprietary - Nexra internal platform.
