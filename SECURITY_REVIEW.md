# Security Review Summary

Date: 2026-05-23

## Runtime Security Controls

1. Shared security response headers are applied in every module `SecurityFilterChain`.
2. Common correlation filter is single-source and validates inbound request IDs.
3. Rate-limit proxy trust is environment-aware (`trust-forwarded-headers=false` by default; enabled explicitly for prod).
4. Auth prod validator fails startup on insecure settings (including missing keystore material).
5. OAuth2 signing in prod is keystore-backed (non-ephemeral).
6. JSON structured logs are enabled for centralized logging pipelines.

## Scan Evidence

1. Command:
   - `.\mvnw.cmd --batch-mode org.owasp:dependency-check-maven:check -DskipTests -DautoUpdate=false`
2. Result:
   - Build PASS
   - No unsuppressed vulnerability findings reported.

## Suppression Review

`dependency-check-suppressions.xml` includes explicit scoped suppressions with notes for:

1. Micrometer Prometheus client false-positive server CVE mapping.
2. Hibernate Validator false-positive CPE mapping.
3. Angus activation contextual suppression.
4. PDFBox example-only CVEs (`CVE-2026-23907`, `CVE-2026-33929`) with non-reachability notes.

## Additional Hardening Added

1. Payroll logo path traversal guard in:
   - `src/main/java/com/nexra/hrms/nexra/modules/payroll/service/impl/PayslipDocumentServiceImpl.java`
2. Regression tests:
   - `src/test/java/com/nexra/hrms/nexra/modules/payroll/PayslipDocumentServiceImplTest.java`
