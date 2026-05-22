# Release RC1 Execution Tracker

Release candidate: `v1.0.0-rc1`  
Commit: `b5f37c5d64bf7576d549ae1d50068fc1f9f5bbdd`  
Date: 2026-05-23

## Current Status

- Build/test baseline: DONE
- Migration compatibility: DONE
- Docker image reproducibility script and evidence: DONE
- Kubernetes deployment manifest baseline: DONE
- Remaining launch blockers: OPEN (operational sign-offs and live environment evidence)

## Blockers To Close Before Go-Live

1. Security CI evidence on release candidate commit
- Owner: Security + Engineering
- Action:
  - Confirm GitHub Actions `security` job PASS for commit `b5f37c5`.
  - Attach run URL to release evidence.
- Evidence target:
  - `build/release/release-evidence.generated.md` section 2.

2. Backup and restore drill in staging
- Owner: DevOps + DBA
- Action:
  - Run `.\build\release\Backup-MySql.ps1 ...`
  - Run approved restore drill via `.\build\release\Restore-MySql.ps1 ... -Force`
  - Archive command output/logs.
- Evidence target:
  - section 3 in `build/release/release-evidence.generated.md`.

3. Observability links and on-call drill
- Owner: SRE/DevOps
- Action:
  - Add metrics/alerts/tracing dashboard URLs.
  - Trigger and acknowledge one synthetic alert.
  - Record responder and timestamps.
- Runbook:
  - `docs/runbooks/observability-oncall-handoff-runbook.md`
- Evidence target:
  - section 5 in `build/release/release-evidence.generated.md`.

4. Performance and resilience sign-off run
- Owner: QA Performance + Engineering
- Action:
  - Execute load/soak/dependency-failure/db-pool tests in staging.
  - Generate summary using:
    - `.\build\release\Generate-PerfResilienceEvidence.ps1 -Environment staging -LoadResult <PASS|FAIL> -SoakResult <PASS|FAIL> -DependencyFailureResult <PASS|FAIL> -DbPoolExhaustionResult <PASS|FAIL> -EvidenceLinks "<links>"`
- Runbook:
  - `docs/runbooks/performance-resilience-validation-runbook.md`
- Evidence target:
  - `build/release/perf-resilience/performance-resilience-evidence.md`
  - section 6 in `build/release/release-evidence.generated.md`.

5. Staging rollout from manifests and rollback proof
- Owner: DevOps
- Action:
  - Apply manifests from `deploy/k8s`
  - Verify readiness/liveness
  - Execute one rollback drill
- Runbook:
  - `docs/runbooks/deployment-manifest-runbook.md`
- Evidence target:
  - section 7 in `build/release/release-evidence.generated.md`.

6. Product/UAT sign-offs
- Owner: Product + QA + Ops
- Action:
  - Complete UAT approval across Auth, HRMS, Payroll, CRM critical workflows.
  - Record final go/no-go approvals.
- Evidence target:
  - sign-off section in `build/release/release-evidence.generated.md`.

## Final Release Command Set

1. Regenerate release evidence after all sections are filled:
- `.\build\release\Generate-ReleaseEvidence.ps1 -ReleaseVersion v1.0.0-rc1 -Environment staging -Approver "Engineering"`

2. Re-run quality gate before go/no-go:
- `.\mvnw.cmd --batch-mode verify`

3. Final release tag (after blockers are closed):
- `v1.0.0` on approved commit

