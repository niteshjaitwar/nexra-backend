# Environment Variable Inventory (Production)

## Scope

Mandatory runtime variables for production deployment of `nexra` backend.

## Core Runtime

- `SPRING_PROFILES_ACTIVE`
- `APPLICATION_NAME`
- `AUTH_DB_URL`
- `AUTH_DB_USERNAME`
- `AUTH_DB_PASSWORD`

## Auth / Security

- `AUTH_JWT_SECRET`
- `AUTH_OAUTH2_ISSUER`
- `AUTH_OAUTH2_DEFAULT_CLIENT_SECRET`
- `AUTH_OAUTH2_KEYSTORE_LOCATION`
- `AUTH_OAUTH2_KEYSTORE_PASSWORD`
- `AUTH_OAUTH2_KEY_ALIAS`
- `AUTH_OAUTH2_KEY_PASSWORD`
- `NEXRA_AUTH_CORS_ALLOWED_ORIGINS`

## Redis / Mail

- `AUTH_REDIS_HOST`
- `AUTH_REDIS_PORT`
- `AUTH_REDIS_PASSWORD`
- `AUTH_MAIL_FROM`
- `AUTH_SMTP_HOST`
- `AUTH_SMTP_PORT`
- `AUTH_SMTP_USERNAME`
- `AUTH_SMTP_PASSWORD`

## Backup / Restore Operations

- `NEXRA_DB_HOST`
- `NEXRA_DB_PORT`
- `NEXRA_DB_NAME`
- `NEXRA_DB_USER`
- `NEXRA_DB_PASSWORD`

## Evidence Rule

For each release candidate, attach:

1. Redacted environment variable sheet per environment (`stage` / `prod`).
2. Secret manager paths/keys (not secret values).
3. Change ticket reference for variable additions/removals.
