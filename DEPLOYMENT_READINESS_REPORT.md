# Deployment Readiness Report

Date: 2026-05-23

## Container Readiness

File: `Dockerfile`

1. Build and runtime base images use pinned digests.
2. Runtime user is non-root.
3. `HEALTHCHECK` is present on `/actuator/health/readiness`.
4. Runtime JVM options are set through `JAVA_TOOL_OPTIONS`.
5. Exec-form entrypoint is used.

## Kubernetes Readiness

File: `deploy/k8s/deployment.yaml`

1. Readiness/liveness/startup probes configured.
2. Resource requests/limits configured.
3. Pod/container security contexts applied (`runAsNonRoot`, dropped capabilities, read-only root FS).
4. Rolling update strategy configured.
5. Image tag moved away from `latest` to explicit release tag.

## Application Prod Profile Readiness

Files:

1. `src/main/resources/application-prod.properties`
2. `src/main/resources/application-auth-prod.properties`

Validated:

1. OpenAPI disabled by default in prod.
2. Centralized rate-limit distributed mode enabled in prod.
3. Actuator surface restricted and stack traces disabled.
4. Auth prod requires keystore env configuration for OAuth2 signing.
