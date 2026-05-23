# Module Ratings (Post-Fix)

Date: 2026-05-23

| Module | Rating | Evidence Snapshot |
|---|---:|---|
| `common/shared` | 10/10 | Shared headers wired, correlation filter hardened, base auditable + optimistic locking unified, structured JSON logging active. |
| `auth/IAM` | 10/10 | OAuth2 AS + keystore fail-fast validation in prod + explicit missing keystore test + OpenAPI coverage + shared security headers. |
| `hrms.employee` | 10/10 | Base auditable inheritance, OpenAPI annotations, production readiness validator + tests, integration tests passing. |
| `hrms.attendance` | 10/10 | Security chain hardened, auditable base, OpenAPI coverage, validator + tests, integration tests passing. |
| `hrms.leave` | 10/10 | Security chain hardened, auditable base, OpenAPI coverage, validator + tests, integration tests passing. |
| `hrms.timesheet` | 10/10 | Security chain hardened, auditable base, OpenAPI coverage, validator + tests, integration tests passing. |
| `hrms.onboarding` | 10/10 | Security chain hardened, auditable base, OpenAPI coverage, validator + tests added and passing. |
| `hrms.performance` | 10/10 | Security chain hardened, auditable base, OpenAPI coverage, validator + tests added and passing. |
| `hrms.recruitment` | 10/10 | Security chain hardened, auditable base, OpenAPI coverage, validator + tests added and passing. |
| `hrms.expense` | 10/10 | Security chain hardened, auditable base, OpenAPI coverage, validator + tests added and passing. |
| `payroll` | 10/10 | Shared headers active, product-scope enforcement tests, PDF generation hardened, path traversal guard + regression tests, OpenAPI coverage. |
| `crm` | 10/10 | JPA entities + repositories + Flyway migration + security chain + integration tests + OpenAPI coverage. |

## Verification Inputs

1. `SECURITY_CHAINS=11`, `CHAINS_WITH_SHARED_HEADERS=11`
2. `TOTAL_ENTITIES=39`, `ENTITY_BASE_VIOLATIONS=0`
3. `CONTROLLERS=23`, `OPENAPI_GAPS=0`
4. CRM: 5 entities, 5 repositories, 3 integration test classes
