# Nexra Modular Monolith Architecture

## Decision

The future product should be built as one Spring Boot application named `nexra` with explicit internal modules.

The existing service folders are treated as source modules for migration, not as the final runtime shape.

## Container

- application name: `nexra`
- root package: `com.nexra.hrms.nexra`
- internal module root: `com.nexra.hrms.nexra.modules`
- shared cross-cutting root: `com.nexra.hrms.nexra.shared`

## Internal Modules

### `modules.auth`

Source folders:
- `auth`

Responsibility:
- identity
- registration and login
- verification and passwordless flows
- JWT and refresh token lifecycle
- tenant provisioning
- product access

### `modules.hrms`

Source folders:
- `employee-core`
- `attendance`
- `leave`
- `timesheet`
- `onboarding`
- `performance`
- `recruitment`

Responsibility:
- employee master data
- attendance and leave
- workforce lifecycle

### `modules.payroll`

Source folders:
- `payroll`
- `expense`

Responsibility:
- payroll setup
- payroll run generation
- statutory deductions
- reimbursement and expense posting

### `modules.crm`

Source folders:
- `crm-leads`
- `crm-contacts`
- `crm-accounts`
- `crm-deals`
- `crm-activities`
- `campaigns-marketing`
- `customer-support`

Responsibility:
- pipeline, customers, support, campaigns

### `modules.admin`

Source folders:
- `master-data`
- `config-service`
- `audit-compliance`
- `workflow-approval`
- `reporting-analytics`

Responsibility:
- platform configuration
- audit and approval policies
- analytics and administration

## Shared Layer

Use `shared/` only for cross-cutting capabilities that are truly generic:

- security filters and token contracts
- tenant context
- request correlation
- common API response model
- common exception model
- outbox/integration primitives
- document abstraction
- notification abstraction

Do not move business logic into `shared/`.

## Migration Rule

When moving a service:

1. preserve its controller/service/repository/config structure
2. move code under the matching internal module package
3. replace network calls between local domains with in-process service calls
4. keep Flyway migrations separated per module path
5. keep module tests and add boundary tests

## First Concrete Moves

1. move `auth` into `modules.auth`
2. move `employee-core` into `modules.hrms.employee`
3. refactor `payroll` to consume employee and organization data in-process

That is the minimum path to a real modular monolith.
