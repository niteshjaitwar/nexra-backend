# Nexra Backend

`nexra` is the modular-monolith backend for the Nexra HRMS/CRM platform.

The goal is to consolidate the earlier microservice-style codebase into one Spring Boot application with clear internal module boundaries, shared platform rules, and production-grade testing.

## Current State

Migrated and running inside this monolith:

- `modules.auth`
- `modules.hrms.employee`
- `modules.hrms.attendance`
- `modules.hrms.leave`
- `modules.hrms.timesheet`
- `modules.hrms.onboarding`
- `modules.hrms.performance`
- `modules.payroll`

Planned next:

- `modules.hrms.recruitment`
- `modules.crm.*`
- `modules.admin.*`

## Tech Stack

- Java 25
- Spring Boot 4
- Spring Security
- Spring Data JPA
- Flyway
- H2 for tests
- MySQL for production
- Redis for auth hardening flows
- Thymeleaf + OpenHTMLToPDF for payslip documents

## Module Layout

```text
src/main/java/com/nexra/hrms/nexra/
  modules/
    auth/
    hrms/
      attendance/
      employee/
      leave/
      performance/
      onboarding/
      timesheet/
    payroll/
  shared/
```

Supporting docs:

- [docs/modular-monolith-architecture.md](./docs/modular-monolith-architecture.md)
- [docs/production-delivery-plan.md](./docs/production-delivery-plan.md)

## Local Run

1. Copy `.env.example` to `.env` or export the environment variables another way.
2. Use the `test` or `dev` Spring profile for local development.
3. Start the app:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=test"
```

## Test

Run the full modular-monolith suite:

```bash
./mvnw verify
```

The current suite covers:

- auth flows and security negatives
- employee-core, attendance, leave, timesheet, onboarding, and performance integration plus actuator smoke
- payroll integration, document generation, and actuator smoke
- monolith context startup

## Production Notes

This repository is being hardened module by module. Security boundaries, request correlation, validation, Flyway discipline, and integration tests are already in place for the migrated modules.

The monolith is the target runtime shape. The older standalone service folders are source material for migration, not the final deployment model.

The delivery order and production gates for the full migration are tracked in [docs/production-delivery-plan.md](./docs/production-delivery-plan.md).
