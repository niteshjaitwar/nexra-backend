# High Level Design

## Architecture Summary

Nexra is a Spring Boot modular monolith. It deploys as one application but is organized into internal business modules with strict package boundaries.

Runtime model:

- one Spring Boot application
- REST APIs
- Spring Security
- OAuth2 Authorization Server
- JWT resource protection
- Spring Data JPA
- Flyway migrations
- MySQL production database
- H2 test database
- Redis for auth hardening flows
- Micrometer and Prometheus metrics
- Actuator health endpoints

## Logical Architecture

```text
Clients
  ├── Web frontend
  ├── Future mobile app
  └── Integration clients
        │
        ▼
Spring Boot Application: nexra
  ├── common
  │   ├── API response model
  │   ├── exception handling
  │   ├── request correlation
  │   ├── security headers
  │   ├── rate limiting
  │   ├── OpenAPI
  │   ├── audit
  │   └── persistence base
  └── modules
      ├── auth
      ├── hrms.employee
      ├── hrms.attendance
      ├── hrms.leave
      ├── hrms.timesheet
      ├── hrms.onboarding
      ├── hrms.performance
      ├── hrms.recruitment
      ├── hrms.expense
      ├── payroll
      ├── crm
      └── admin
        │
        ▼
Infrastructure
  ├── MySQL
  ├── Redis
  ├── SMTP
  ├── Object storage, future
  └── Monitoring stack
```

## Deployment Topology

```text
Users and integration clients
        │
        ▼
DNS / CDN / WAF
        │
        ▼
Load balancer or ingress
        │
        ▼
Nexra application instances
  ├── instance A: Spring Boot modular monolith
  ├── instance B: Spring Boot modular monolith
  └── instance N: Spring Boot modular monolith
        │
        ├── MySQL primary database
        ├── Redis for auth hardening and rate-limit support
        ├── SMTP provider
        ├── Object storage, future documents
        └── Monitoring stack: logs, metrics, alerts
```

Environment boundary:

```text
Local -> Dev -> E2E -> Stage -> Production
```

Release readiness sequence:

```text
build -> test -> stage -> release candidate -> production
```

Production instances must be stateless. Shared state belongs in MySQL, Redis, object storage, or external service providers.

## Module Responsibilities

| Module | Responsibility |
| --- | --- |
| common | Shared API, exceptions, audit, metrics, rate limiting, correlation, headers, persistence helpers. |
| auth | Tenants, users, verification, tokens, OAuth clients, product access. |
| hrms.employee | Organization profile, departments, employees. |
| hrms.attendance | Shifts, check-in/out, attendance records. |
| hrms.leave | Leave types, holidays, balances, requests, approvals. |
| hrms.timesheet | Projects, entries, approval workflow, summaries. |
| hrms.onboarding | Plans, tasks, completion, onboarding summary. |
| hrms.performance | Goals, reviews, completion, summary. |
| hrms.recruitment | Jobs, candidates, stage changes, history. |
| hrms.expense | Categories, claims, claim items, approval, reimbursement. |
| payroll | Payroll profiles, payroll slips, payslip documents. |
| crm | Leads and future sales/customer domain records. |
| admin | Future platform administration, workflow, reporting, master data. |

## Shared Service Boundaries

| Shared Service | Current State | Boundary Rule |
| --- | --- | --- |
| Audit | Implemented as common audit event foundation. | Modules emit audit events, but audit storage stays shared. |
| Rate limiting | Implemented as common filter. | Modules should not implement separate request throttling unless product-specific limits are required. |
| Correlation | Implemented through request correlation filter. | Every request must preserve or create `X-Request-Id`. |
| Notification | Future shared service. | Modules request notification delivery through a contract, not SMTP directly. |
| Documents | Future shared service. | Modules store metadata and object references, not raw files in business tables. |
| Workflow | Future shared service. | Modules own business state; workflow owns approval routing and policy. |
| Reporting | Future shared read model. | Reporting reads stable module contracts or read models, not controller responses. |

## Request Flow

1. Client sends REST request.
2. Correlation filter assigns or preserves `X-Request-Id`.
3. Security filter validates token and extracts tenant/user context.
4. Controller validates request DTO.
5. Service applies business rules, authorization, and tenant constraints.
6. Repository reads or writes module-owned tables.
7. Audit event is recorded for high-value action.
8. Response is wrapped in the common API response format.
9. Metrics and logs are emitted with request correlation.

## Design Principles

- Controllers remain thin.
- Services own business behavior.
- Repositories own persistence only.
- Modules own their domain tables.
- Cross-module behavior should use service contracts, not direct repository access.
- Flyway owns database evolution.
- Production configuration is secret-safe and externally supplied.

## Scalability Approach

The MVP uses a modular monolith to reduce operational complexity. Scale vertically first and horizontally by running multiple stateless application instances behind a load balancer. Keep user sessions token-based and externalize shared state to MySQL/Redis.

Future decomposition into services should be considered only after module boundaries, traffic volume, team ownership, and data contracts justify it.
