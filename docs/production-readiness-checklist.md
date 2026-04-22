# Nexra Production Readiness Checklist (10/10 Target)

This checklist defines the acceptance criteria for each module to be rated 10/10 production-ready.

## Global Acceptance Criteria

- Shared infrastructure from `common` is used consistently (no module duplicates for cross-cutting concerns).
- Every mutable JPA entity extends `common.persistence.BaseAuditableEntity` and uses optimistic locking.
- Security chains apply shared security headers and consistent authz patterns.
- API contracts are standardized on `common.api.ApiResponse`.
- Every public endpoint has OpenAPI annotations (`@Operation`, `@ApiResponses`).
- Every module has production-readiness validator coverage and tests.
- Full build and tests pass (`mvnw clean test`).

## Module Checklist

### auth
- [ ] Shared security headers customizer applied in chain
- [ ] Module correlation filter duplication removed or delegated to common filter
- [ ] Entity base migrated to common auditable base
- [ ] OpenAPI coverage completed for all public auth endpoints
- [ ] Product-scope and tenant authz checks validated by tests

### payroll
- [ ] Shared security headers customizer applied in chain
- [ ] Module correlation filter duplication removed or delegated to common filter
- [ ] Entity base migrated to common auditable base
- [ ] Product-scope claim enforcement added and tested
- [ ] OpenAPI coverage completed for all public payroll endpoints

### employee
- [ ] Entity base migrated to common auditable base
- [ ] Validation and error contract consistency maintained
- [ ] OpenAPI coverage completed

### attendance
- [ ] Entity base migrated to common auditable base
- [ ] Validation and error contract consistency maintained
- [ ] OpenAPI coverage completed

### leave
- [ ] Entity base migrated to common auditable base
- [ ] Validation and error contract consistency maintained
- [ ] OpenAPI coverage completed

### timesheet
- [ ] Entity base migrated to common auditable base
- [ ] Validation and error contract consistency maintained
- [ ] OpenAPI coverage completed

### onboarding
- [ ] Entity base migrated to common auditable base
- [ ] Repository-level filtering/pagination completed
- [ ] OpenAPI coverage completed

### performance
- [ ] Entity base migrated to common auditable base
- [ ] Repository-level filtering/pagination completed
- [ ] OpenAPI coverage completed

### recruitment
- [ ] Entity base migrated to common auditable base
- [ ] Repository-level filtering/pagination completed
- [ ] OpenAPI coverage completed

### expense
- [ ] Entity base migrated to common auditable base
- [ ] Expense production-readiness validator added with tests
- [ ] OpenAPI coverage completed

### crm
- [ ] Persistence implemented (JPA + Flyway) for lead domain
- [ ] Security chain implemented and `nexra.crm.enforce-auth` wired
- [ ] Integration tests added for CRUD/authz/pagination
- [ ] OpenAPI coverage completed
