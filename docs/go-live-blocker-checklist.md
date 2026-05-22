# Nexra Go-Live Blocker Checklist

As of: 2026-05-23

This is the minimum mandatory list for enterprise launch approval. Any open item below is a launch blocker.

## 1) Release Candidate Integrity

- [x] `mvnw verify` is green on release commit.
- [x] Flyway clean migration and previous-version migration checks pass.
- [x] Release evidence file generated for the candidate commit.
- [ ] Release tag created and immutable build artifact checksum recorded.
Owner: Engineering

## 2) Security Gate

- [ ] CI `security` job PASS for release commit (CodeQL, Trivy, Gitleaks).
- [ ] Auth negative and tenant isolation test evidence attached in release pack.
- [ ] Secrets source moved to managed secret store in target environment.
- [ ] Secret rotation drill completed and recorded.
Owner: Security + Engineering

## 3) Data Safety Gate

- [ ] Staging backup run evidence attached (`Backup-MySql.ps1` output/log).
- [ ] Staging restore drill evidence attached (`Restore-MySql.ps1` output/log).
- [ ] Retention/deletion policy sign-off from compliance/data owner.
Owner: DevOps + DBA + Compliance

## 4) Observability And On-Call Gate

- [ ] Metrics dashboard URL(s) linked in release evidence.
- [ ] Alerts dashboard URL(s) linked in release evidence.
- [ ] Tracing dashboard URL(s) linked in release evidence.
- [ ] Synthetic alert routing drill executed; ack/resolve timestamps recorded.
- [ ] Runbook links verified reachable by on-call team.
Owner: SRE/DevOps

## 5) Performance And Resilience Gate

- [ ] Load test report for target launch capacity attached.
- [ ] Soak test report attached (minimum 4h sustained run).
- [ ] Dependency degradation test results attached (redis/mail/storage).
- [ ] DB pool exhaustion behavior verified and attached.
- [ ] Pass/fail reviewed against launch SLO thresholds.
Owner: QA Performance + Engineering

## 6) Deployment Gate

- [x] Dockerfile present and reproducible-image script available.
- [x] Kubernetes manifest baseline present in `deploy/k8s`.
- [ ] Environment-specific secrets manifest prepared in cluster (not example file).
- [ ] First staging rollout from manifests completed and rollout/rollback logs attached.
Owner: DevOps

## 7) Product And Business Sign-Off

- [ ] End-to-end UAT sign-off across Auth, HRMS, Payroll, CRM modules.
- [ ] Payroll and compliance stakeholders approve release notes.
- [ ] Support/operations handoff complete with escalation matrix.
Owner: Product + QA + Ops

## 8) Final Go/No-Go

- [ ] Go/No-Go meeting held with Eng, QA, Security, DevOps, Product.
- [ ] All blockers above closed.
- [ ] Signed release decision documented in release evidence.
Owner: Release Manager

