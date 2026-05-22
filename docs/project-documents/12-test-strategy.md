# Test Strategy

## Objective

Verify that Nexra is functionally correct, secure, tenant-isolated, observable, and release-ready for commercial pilots.

## Test Levels

### Unit Tests

Purpose:

- business rules
- validators
- mappers
- service decisions
- money calculations

Required for:

- payroll calculations
- leave balance decisions
- approval state transitions
- token and password validation helpers

### Integration Tests

Purpose:

- controller, service, repository, and database behavior
- Flyway migrations
- API request and response contracts
- module security filters

Required for each module:

- success path
- validation failure
- unauthorized request
- forbidden request
- cross-tenant denial
- duplicate business key conflicts

### Contract Tests

Purpose:

- ensure frontend and integration clients can rely on API behavior
- detect response shape changes

Artifacts:

- OpenAPI docs
- API examples
- response envelope standards

### End-To-End Tests

Minimum workflows:

- tenant provisioning and product access
- register, verify, login, refresh, logout
- employee creation and department setup
- attendance check-in and check-out
- leave request and approval
- timesheet submission and approval
- onboarding plan and task completion
- payroll profile setup, generation, payslip retrieval
- expense claim approval and reimbursement
- recruitment candidate stage movement
- CRM lead lifecycle

### Security Tests

Coverage:

- authentication failure
- authorization failure
- tenant isolation failure attempts
- product access denial
- sensitive data not present in logs
- rate limiting
- token expiry and refresh
- dependency scanning
- secret scanning
- API DAST before launch

### Performance Tests

Initial launch targets:

- login and token refresh under expected pilot load
- employee list and summary queries with realistic tenant data
- payroll generation for pilot tenant size
- attendance check-in bursts
- CRM lead list filters

### Regression Tests

Run before every release:

```powershell
.\mvnw.cmd --batch-mode verify
```

Regression suite must include:

- all unit tests
- integration tests
- production readiness validator tests
- OpenAPI compliance tests
- JaCoCo coverage gate
- Maven enforcer rules

## Test Data Strategy

- Use generated test tenants per test class or workflow.
- Avoid shared mutable test data.
- Use H2 for automated tests unless MySQL-specific behavior is under test.
- Use deterministic IDs and dates where assertions require stability.
- Do not use production data in lower environments.

## Defect Severity

| Severity | Definition |
| --- | --- |
| Critical | Security breach, cross-tenant exposure, payroll corruption, data loss, app unavailable. |
| High | Major workflow blocked, authorization bypass, inconsistent financial or HR data. |
| Medium | Important workflow degraded with workaround. |
| Low | Cosmetic, copy, non-blocking usability or documentation issue. |

## Traceability Matrix

| Business Requirement | Product Area | Required Test Coverage | Current Evidence Target |
| --- | --- | --- | --- |
| BR-001 Tenant provisioning | Auth/platform | integration, authorization, validation, audit | platform tenant tests |
| BR-002 Register, verify, login, refresh, logout | Auth | unit, integration, negative security, rate limit | auth flow tests |
| BR-003 Product access management | Auth/admin | integration, authorization, cross-tenant denial | product access tests |
| BR-004 Organization, departments, employees | Employee core | integration, validation, duplicate constraints, tenant isolation | employee core tests |
| BR-005 HRMS workflows | Attendance, leave, timesheet, onboarding, performance, recruitment, expense | integration, E2E, approval negative paths | module integration tests |
| BR-006 Payroll profiles and payslips | Payroll | unit, integration, document rendering, authorization, money precision | payroll service and integration tests |
| BR-007 CRM leads | CRM | integration, validation, tenant isolation, duplicate lead email | CRM lead tests |
| BR-008 Audit sensitive actions | Common audit, all modules | integration and audit event assertions | audit event tests |
| BR-009 Incremental module launch | Architecture/DevOps | module smoke tests, product access checks | release smoke suite |
| BR-010 Future extensibility | Architecture | architecture review, migration review, boundary tests | HLD and code review gates |

Traceability rule: every launch-critical business requirement must map to at least one automated test class or a documented manual release test before production.

## Release Test Exit Criteria

- zero open critical defects
- zero open high security defects
- all MVP critical workflows pass
- shared backend quality gate passes: `.\mvnw.cmd --batch-mode verify`
- migration from clean database passes
- rollback procedure tested
- backup and restore drill completed

## Operations Severity Alignment

Use the same support severity language as operations:

| Severity | QA Meaning | Operations Meaning |
| --- | --- | --- |
| SEV1 | Blocks release or exposes data/security/payroll corruption risk. | Data breach, cross-tenant exposure, production unavailable, payroll corruption. |
| SEV2 | Major workflow unavailable with no acceptable workaround. | Major workflow outage without broad Nexra outage. |
| SEV3 | Workflow degraded with workaround. | Degraded performance or partial module failure. |
| SEV4 | Minor defect, copy, documentation, or support issue. | Minor issue or support defect. |

## Test Gaps To Track

- country-specific payroll statutory tests
- browser E2E tests for future frontend
- load test thresholds by paid plan
- chaos and resilience testing
- long-term audit retention tests
