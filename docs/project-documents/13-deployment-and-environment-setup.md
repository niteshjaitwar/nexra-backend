# Deployment And Environment Setup

## Runtime Stack

- Java 25
- Maven 3.9 or later
- Spring Boot 4
- MySQL for runtime
- H2 for tests
- Redis for auth hardening flows
- SMTP for email verification
- Prometheus-compatible metrics

## Local Development

From repository root:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Default local frontend expectation:

```text
http://localhost:8081
```

Run quality gates:

```powershell
.\mvnw.cmd --batch-mode verify
```

## Environment Profiles

| Profile | Purpose |
| --- | --- |
| `local` | Developer workstation overrides. |
| `dev` | Shared development defaults. |
| `test` | Automated test profile. |
| `e2e` | End-to-end workflow testing. |
| `stage` | Release candidate validation. |
| `prod` | Production runtime. |

Module-specific profile files exist for auth, employee, attendance, leave, timesheet, onboarding, performance, recruitment, expense, payroll, and CRM.

## Required Environment Variables

Core:

```text
SPRING_PROFILES_ACTIVE=prod
APPLICATION_NAME=nexra
AUTH_DB_URL=jdbc:mysql://<host>:3306/nexra
AUTH_DB_USERNAME=<user>
AUTH_DB_PASSWORD=<secret>
```

Auth:

```text
AUTH_JWT_SECRET=<32-byte-minimum-secret>
AUTH_OAUTH2_ISSUER=https://<api-domain>
AUTH_OAUTH2_DEFAULT_CLIENT_SECRET=<secret>
AUTH_OAUTH2_KEYSTORE_LOCATION=<path-or-secret-ref>
AUTH_OAUTH2_KEYSTORE_PASSWORD=<secret>
AUTH_OAUTH2_KEY_ALIAS=<alias>
AUTH_OAUTH2_KEY_PASSWORD=<secret>
NEXRA_AUTH_CORS_ALLOWED_ORIGINS=https://<frontend-domain>
```

Redis and mail:

```text
AUTH_REDIS_HOST=<host>
AUTH_REDIS_PORT=6379
AUTH_REDIS_PASSWORD=<secret>
AUTH_MAIL_FROM=noreply@<domain>
AUTH_SMTP_HOST=<host>
AUTH_SMTP_PORT=587
AUTH_SMTP_USERNAME=<user>
AUTH_SMTP_PASSWORD=<secret>
```

## Database Setup

1. Create MySQL database.
2. Create least-privilege application user.
3. Configure datasource environment variables.
4. Start application.
5. Flyway applies migrations automatically.
6. Verify actuator health and startup logs.

## Deployment Steps

1. Build artifact:

```powershell
.\mvnw.cmd --batch-mode clean package
```

2. Publish artifact to release storage or container registry.
3. Deploy to stage.
4. Run smoke tests and regression checks.
5. Promote same artifact to production.
6. Verify health, metrics, logs, and key workflows.

## Production Checklist

- No default secrets.
- Production profile active.
- Database backup enabled.
- Redis configured for auth hardening.
- SMTP configured or notification fallback explicitly approved.
- CORS restricted to approved frontend origins.
- TLS terminated at load balancer or ingress.
- Actuator exposure reviewed.
- Logs and metrics shipped to monitoring system.
- Rollback artifact available.

## Rollback

Rollback must consider application and database separately.

Application rollback:

- redeploy previous known-good artifact
- verify health
- verify auth and core workflows

Database rollback:

- Flyway migrations should be forward-compatible where possible
- destructive migrations require explicit rollback plan
- restore from backup only after incident commander approval

## Smoke Tests

After deployment:

- application starts
- health endpoint is green
- OpenAPI endpoint loads
- login succeeds
- token refresh succeeds
- employee summary loads
- payroll status loads
- CRM lead list loads
- metrics endpoint emits data
