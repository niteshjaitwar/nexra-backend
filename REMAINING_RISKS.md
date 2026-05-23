# Remaining Risks And Follow-Ups

Date: 2026-05-23

## Open Items

1. PDFBox example CVEs are currently suppressed with justification because `3.0.8+` is not yet available on Maven Central.  
   Follow-up: upgrade `org.apache.pdfbox` artifacts to `3.0.8+` when released.
2. Dependency-check prints a Lucene runtime warning on Java 25 about Vector API optimization.  
   Impact: no build failure, no runtime risk for Nexra application; tooling-only warning.
3. Final production rollout still requires environment-level controls:
   - secret manager/K8s secret provisioning
   - TLS and ingress policy validation
   - staged rollout and rollback rehearsal

## Risk Posture

Current codebase passes build, tests, and scan gates with hardened security defaults and fail-fast production validations enabled.
