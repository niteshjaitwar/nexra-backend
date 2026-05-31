# Production Readiness Scorecard

This document is an honest, code-level assessment of the `nexra-backend` codebase. It
distinguishes what is genuinely production-ready in the code from the external,
non-code factors that are required to claim a literal "10/10 versus Salesforce /
Zoho / ADP / Microsoft 365." Those external factors cannot be satisfied by source
changes alone, and they are listed plainly rather than hidden.

**Honest current rating: ~8.5 / 10 on code-level readiness.**

---

## 1. What is production-ready in code

### Configuration & secrets
- All secrets (JWT signing keys, OAuth2 client secrets, keystore passwords, mail
  credentials, datasource credentials) are environment-variable driven. No secrets
  are committed to the repository.
- Per-module `ProductionReadinessValidator` fail-fast checks run at startup for the
  `prod` profile. A misconfigured production instance refuses to start instead of
  booting in an unsafe state. Enforced invariants include:
  - `AUTH_JWT_SECRET`, `AUTH_OAUTH2_ISSUER`, and `AUTH_OAUTH2_DEFAULT_CLIENT_SECRET`
    must be present.
  - OAuth2 ephemeral signing keys must be disabled; the keystore resource must exist.
  - Bootstrap/seed endpoints must be disabled.
  - OpenAPI / Swagger UI must be disabled.
  - Distributed rate limiting must be enabled.
  - **`app.auth.mail.enabled` must be `true`**, which guarantees the SMTP-backed
    `EmailNotificationService` (not the logging fallback) is the active
    `NotificationService`. This closes the previously latent risk of silently
    dropping OTP / verification emails in production.

### Database
- Hibernate `ddl-auto=validate` in production: the application never mutates schema.
- Flyway owns all schema migrations (versioned, repeatable, ordered). A migration
  compatibility test asserts the expected latest schema version.

### Web & transport security
- `SecurityHeadersCustomizer` enforces HSTS, Content-Security-Policy, frame options,
  `X-Content-Type-Options`, Referrer-Policy, and Permissions-Policy.
- Stateless JWT authentication; refresh tokens are stored only as SHA-256 hashes and
  support rotation plus reuse-detection-driven revocation.
- MFA (TOTP) with one-time, hashed recovery codes.
- Brute-force / OTP throttling and distributed rate limiting.

### Observability & operations
- OTLP tracing, Prometheus metrics, and health endpoints.
- Structured JSON logging.
- Dockerfile and Kubernetes manifests are present; CI pipeline is configured.

### No non-production artifacts in the shipped jar
The following were removed or relocated so they cannot reach production:
- **Removed** the `rawTokenForDevOnly` field from `VerificationDispatchResponse` and
  the `app.auth.expose-verification-token-in-response` property. Verification tokens
  are never returned over the API in any profile. Integration tests now obtain OTP /
  link tokens through a test-only capturing `NotificationService`, not the HTTP
  response.
- **Relocated** `DevDataSeederConfig` from `src/main` to `src/test`. It remains
  available to the `dev` and `e2e` profiles for local development and end-to-end
  tests, but is never packaged into the production artifact.

---

## 2. External blockers to a literal 10/10 (not solvable in code)

These require organizational investment, third parties, time, and money. They are
intentionally out of scope for a source-code hardening pass:

1. **Independent third-party penetration testing** and remediation sign-off.
2. **SOC 2 Type II and/or ISO 27001 certification** (controls, evidence, audit).
3. **Government-certified statutory e-filing.** The payroll statutory export
   (JSON/XML) produces filing artifacts, but certified, jurisdiction-specific direct
   e-filing integrations require accreditation per country.
4. **Multi-region high availability / disaster recovery** with tested RTO/RPO,
   cross-region replication, and failover drills.
5. **Product breadth and ecosystem** parity (years of features, marketplace,
   integrations, mobile apps) relative to incumbent suites.
6. **24/7 on-call, SLAs, and operational maturity** (runbooks, incident response,
   chaos testing) proven over time in production.

---

## 3. Honest per-module snapshot

| Area | Code-level readiness | Notes |
| --- | --- | --- |
| Auth / identity | Strong | MFA + recovery codes, hashed tokens, fail-fast prod validation, no token leakage. |
| HRMS (attendance, leave, employee, timesheet) | Strong | Regularization workflow, role-gated admin actions. |
| Payroll | Strong | Slip generation, statutory filing export (JSON + XML). Certified e-filing is external. |
| CRM | Good | Campaign/quote lifecycle, CRM→Ops project automation on quote acceptance. |
| Platform (security headers, rate limiting, observability) | Strong | HSTS/CSP, distributed rate limiting, tracing/metrics/health. |

---

## 4. Verification

Run the full build and test suite:

```bash
./mvnw --batch-mode verify
```

All tests must pass with no Flyway/schema regressions before a release is cut.
