# Auth And Security Design

## Security Objectives

- Protect tenant data from cross-tenant access.
- Enforce product access and role-based access.
- Secure authentication, verification, and refresh token flows.
- Prevent sensitive data exposure in logs, responses, and metrics.
- Provide auditability for high-value business actions.
- Align backend controls with OWASP API Security, OWASP ASVS, NIST SSDF, and CIS Controls guidance.

## Identity Model

Primary entities:

- tenant
- user account
- user role
- product access
- OAuth client
- verification token
- refresh token

## Authentication

Supported flows:

- user registration
- login
- access token issue
- refresh token rotation/reissue
- logout/revocation
- OTP verification
- link verification
- OAuth client management

Protected APIs require bearer token authentication. Tokens must carry enough identity context to resolve:

- user id
- subject/email
- tenant
- roles
- product access where required

## Authorization

Authorization layers:

1. Authenticated request required for protected APIs.
2. Tenant isolation on every business read and write.
3. Role-based permission checks for admin, HR, payroll, finance, manager, employee, and sales actions.
4. Product access checks for enabled Nexra modules.
5. Object-level authorization for records such as employee, claim, payroll slip, candidate, and lead.

## Tenant Isolation

Tenant isolation rules:

- Every tenant-owned business table must include tenant context or tenant linkage.
- Service methods must constrain queries by tenant.
- Mutations must validate record ownership before update or delete.
- Tests must include negative cross-tenant cases.
- Tenant code normalization must be consistent.

## Secrets And Tokens

Rules:

- JWT signing secrets must be at least 32 bytes for HMAC.
- Production secrets must come from environment variables or secret manager.
- Refresh tokens and verification tokens must be stored as hashes.
- Tokens, OTPs, passwords, private identifiers, and payroll data must not be logged.
- OAuth keys and client secrets must not be committed.

## Security Controls

| Control | Design |
| --- | --- |
| Password strength | Bean validation and strong password validator. |
| Verification abuse | OTP/link request flows must be rate-limited. |
| Login abuse | Redis-backed protection in production where enabled. |
| API abuse | Global rate limiting through common filter. |
| Security headers | Common security header customizer. |
| CORS | Explicit allowed origins per environment. |
| Audit | High-value mutations write append-only audit events. |
| Correlation | `X-Request-Id` propagated in request handling. |
| Error handling | JSON `401`, `403`, validation, conflict, and not-found responses. |

## Sensitive Data Classification

High sensitivity:

- passwords and token material
- payroll data
- bank, tax, statutory identifiers
- employee personal data
- audit events
- admin grants and product access
- verification codes and links

Medium sensitivity:

- attendance
- leave
- timesheets
- expenses
- recruitment candidate data
- CRM customer data

## Audit Requirements

Audit these actions:

- tenant provisioning
- product access grant/revoke
- user role and OAuth client changes
- employee create/update
- leave approval/rejection
- timesheet approval/rejection
- expense approval/rejection/reimbursement
- payroll generation and payslip access
- CRM lead deletion or conversion
- security-sensitive reads where required

## Security Test Requirements

- authentication success and failure
- authorization negative paths
- product access denial
- cross-tenant access denial
- validation failure
- rate limit behavior
- token expiration and refresh behavior
- secret scanning
- dependency scanning
- API DAST before launch

## Production Security Gates

- No default secrets in production.
- Production startup fails for missing critical secrets.
- CORS only allows approved origins.
- Actuator exposure is limited and protected where required.
- Database users follow least privilege.
- TLS is enforced at the edge.
- Backups are encrypted.
- Logs are retained securely.
