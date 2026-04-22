# Nexra Backend

Production-grade modular monolith powering **Nexra HRMS + CRM**.

## Overview

Nexra Backend is a single Spring Boot runtime with strict internal module boundaries, shared cross-cutting standards, and CI-enforced quality gates.  
It is designed for fast feature delivery without microservice complexity while preserving enterprise reliability.

- **Live website**: [https://hrms.nexra.info](https://hrms.nexra.info)
- **Architecture**: Modular monolith
- **Primary domain**: HRMS + CRM
- **Current status**: Production-ready backend baseline with verified module hardening

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
- `crm` (tenant-isolated persistent lead baseline + domain schema for accounts/contacts/deals/activities/tasks)

## Core Engineering Standards

- Shared API contract via canonical `ApiResponse`
- Shared exception model with centralized global handling
- Shared request correlation (`X-Request-Id`) and hardened security headers
- Shared global rate limiting with bounded in-memory key strategy
- Shared auditing and optimistic locking (`@Version`) via common persistence base
- Flyway-first schema evolution
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

## Verify Quality Gates

```bash
./mvnw verify
```

This executes:
- compile + unit/integration tests
- module production validator tests
- JaCoCo coverage check
- enforcer baseline rules

## Frontend Integration Notes

- Frontend workspace (`../nexrahrms`) targets this monolith backend.
- Local frontend expects backend on `http://localhost:8081`.
- API base paths:
  - `/api/v1/auth/*`
  - `/api/v1/employee-core/*`
  - `/api/v1/attendance/*`
  - `/api/v1/leave/*`
  - `/api/v1/timesheet/*`
  - `/api/v1/payroll/*`
  - `/api/v1/crm/*`

## Documentation

- [Modular Monolith Architecture](./docs/modular-monolith-architecture.md)
- [Production Delivery Plan](./docs/production-delivery-plan.md)
- [Production Readiness Checklist](./docs/production-readiness-checklist.md)

## License

Proprietary - Nexra internal platform.
