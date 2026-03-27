# Nexra Production Delivery Plan

## Purpose

This document defines how the existing service-based codebase will be consolidated into the `nexra` modular monolith and what must be true before any module is called production ready.

This is a delivery program, not a marketing statement. No module or platform area should be marked complete until it clears the quality gates in this file.

## Current Monolith Status

Migrated into `nexra`:

- `modules.auth`
- `modules.hrms.employee`
- `modules.hrms.attendance`
- `modules.hrms.leave`
- `modules.hrms.timesheet`
- `modules.hrms.onboarding`
- `modules.hrms.performance`
- `modules.hrms.recruitment`
- `modules.payroll`

Still pending migration:

- `expense`
- `crm-leads`
- `crm-contacts`
- `crm-accounts`
- `crm-deals`
- `crm-activities`
- `campaigns-marketing`
- `customer-support`
- `master-data`
- `audit-compliance`
- `workflow-approval`
- `reporting-analytics`
- `documents`
- `file-storage`
- `notification`
- `search-index`

Supporting runtime concerns to absorb into the monolith platform:

- `api-gateway`
- `config-service`
- `service-registry`
- `integration-events`

Out-of-scope folders for backend migration planning:

- `UI`
- `ahub-*`

## Target Module Map

### `modules.hrms`

Source folders:

- `employee-core`
- `attendance`
- `leave`
- `timesheet`
- `onboarding`
- `performance`
- `recruitment`
- `documents`

### `modules.payroll`

Source folders:

- `payroll`
- `expense`

### `modules.crm`

Source folders:

- `crm-leads`
- `crm-contacts`
- `crm-accounts`
- `crm-deals`
- `crm-activities`
- `campaigns-marketing`
- `customer-support`

### `modules.admin`

Source folders:

- `master-data`
- `audit-compliance`
- `workflow-approval`
- `reporting-analytics`

### `shared`

Cross-cutting source folders:

- `notification`
- `file-storage`
- `search-index`
- `integration-events`

`api-gateway`, `config-service`, and `service-registry` are not target business modules in the monolith. Their useful capabilities must be internalized as application configuration, security/filtering, shared infrastructure, or deployment concerns.

## Production-Ready Baseline For Every Module

Every migrated module must match the `auth` standard before it is treated as release-candidate quality.

### Code Structure

- package layout aligned with `auth`
- controller, service, repository, config, dto, exception, validation separation
- explicit module configuration class
- no accidental bean-name collisions across modules
- no dead standalone-service bootstrap classes left inside the monolith

### Security

- tenant isolation enforced in code paths and tests
- role and permission checks enforced in controller and service boundaries
- JSON `401` and `403` handling aligned with the monolith standard
- request correlation with `X-Request-Id`
- rate limiting where abuse is plausible
- no sensitive data in logs
- production startup validation for critical secrets and security settings

### Reliability

- persistence moved off temporary or reference-only in-memory paths
- Flyway migrations added for real schema ownership
- profile-specific configuration for `test`, `dev`, `e2e`, and `prod`
- readiness and liveness actuator groups exposed
- failure modes mapped to consistent API responses

### Operability

- audit events for high-value business actions
- metrics emitted for critical workflows
- logs structured enough for correlation and incident analysis
- clear environment variables documented
- CI executes `./mvnw verify`

### Quality Gates

- unit tests for domain logic
- integration tests for happy path, negative path, and authorization path
- E2E tests for critical business workflows
- smoke coverage for actuator endpoints
- performance and abuse-path checks for public or expensive endpoints

## Execution Order

The order below is mandatory because it reduces rework and stabilizes shared contracts early.

### Phase 1: Harden Existing Monolith Modules

1. `modules.auth`
2. `modules.hrms.employee`
3. `modules.hrms.attendance`
4. `modules.payroll`

Required outcomes:

- remove remaining reference-only storage where it exists
- align comments, authorship headers, DTO conventions, and exception style
- finish module-level rate limiting, caching, and audit coverage
- complete E2E workflows for each migrated module

### Phase 2: Complete Core HRMS

1. `leave`
2. `timesheet`
3. `onboarding`
4. `performance`
5. `recruitment`
6. `documents`

Required outcomes:

- replace inter-service calls with in-process service contracts
- unify employee and tenant references with existing monolith modules
- add schema ownership and workflow tests

### Phase 3: Complete Payroll Domain

1. `expense`
2. statutory and payroll-run hardening
3. payroll-to-employee integration verification

Required outcomes:

- payroll persistence is durable and auditable
- expense posting integrates cleanly with payroll cycles
- document generation and export flows are stable under load

### Phase 4: Complete CRM

1. `crm-leads`
2. `crm-contacts`
3. `crm-accounts`
4. `crm-deals`
5. `crm-activities`
6. `campaigns-marketing`
7. `customer-support`

Required outcomes:

- shared customer and tenant views are normalized
- authorization and audit rules are consistent across sales and support flows
- search and activity timelines are internally integrated

### Phase 5: Complete Admin And Shared Platform

1. `master-data`
2. `audit-compliance`
3. `workflow-approval`
4. `reporting-analytics`
5. `notification`
6. `file-storage`
7. `search-index`
8. absorb `integration-events` capabilities into shared platform code

Required outcomes:

- approval and audit policies are centrally enforceable
- reporting reads from stable internal contracts
- storage and notification abstractions are reusable by all modules

### Phase 6: Platform Release Gates

This is where platform-level production claims are earned.

- full `./mvnw verify`
- full E2E suite across auth, HRMS, payroll, CRM, and admin flows
- migration test from clean database
- seed-free production startup validation
- secret-management integration
- backup and restore procedure
- deployment manifests and rollback procedure
- load, resilience, and abuse-path verification
- observability baseline: logs, metrics, tracing, alerts

## End-To-End Workflow Minimums

At minimum, the final monolith must automatically verify these workflows:

- tenant provisioning and product access
- user registration, verification, login, and refresh flow
- employee creation and department/profile management
- attendance mark flow
- leave request and approval flow
- timesheet submission and approval flow
- onboarding lifecycle flow
- payroll profile setup, payroll generation, and payslip retrieval
- expense submission and payroll linkage
- CRM lead to deal progression
- support ticket lifecycle
- approval workflow escalation
- audit trail retrieval
- reporting read flows for core operational dashboards

## Definition Of Done

A module is done only when:

- it runs only inside `nexra`
- its database state is migration-controlled
- its critical workflows have automated test coverage
- its auth, tenant, audit, and validation behavior match the platform standard
- it does not depend on retired microservice-only infrastructure patterns

The platform is done only when every migrated module clears its own quality gates and the platform release gates also pass together.
