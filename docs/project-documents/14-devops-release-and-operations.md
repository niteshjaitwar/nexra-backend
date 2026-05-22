# DevOps Release And Operations

## Operational Goals

- Deploy repeatably.
- Detect incidents quickly.
- Recover safely.
- Protect customer data.
- Keep release quality measurable.

## Environments

| Environment | Purpose | Data |
| --- | --- | --- |
| Local | Developer work | Synthetic only |
| Dev | Shared integration | Synthetic only |
| E2E | Automated workflow testing | Synthetic only |
| Stage | Production-like release validation | Masked or synthetic |
| Prod | Customer runtime | Customer data |

## CI Pipeline

Required steps:

1. checkout
2. Java 25 setup
3. dependency cache restore
4. compile
5. unit and integration tests
6. JaCoCo report and coverage check
7. Maven enforcer
8. OpenAPI compliance test
9. artifact build
10. artifact publish

Required command:

```powershell
.\mvnw.cmd --batch-mode verify
```

## Release Process

1. Build artifact.
2. Test with the shared quality gate: `.\mvnw.cmd --batch-mode verify`.
3. Deploy to stage.
4. Approve release candidate.
5. Deploy to production.
6. Monitor health, logs, metrics, and business workflows.
7. Publish release notes.

## Release Calendar

Recommended pilot cadence:

| Event | Timing | Owner |
| --- | --- | --- |
| Sprint planning | Every second Monday | Product owner |
| Scope freeze | Sprint day 7 | Product owner and tech lead |
| Code freeze | Sprint day 9 | Tech lead |
| Regression window | Sprint days 9-10 | QA |
| Stage deployment | Sprint day 10 | DevOps |
| Release candidate review | Sprint day 10 | Product, engineering, QA, DevOps |
| Production deployment | Approved release window only | DevOps |
| Post-release monitoring | First 24 hours after deploy | DevOps and engineering |

Emergency releases may bypass the normal calendar only with product owner, tech lead, QA, and DevOps approval.

## Operations RACI

| Activity | Product Owner | Tech Lead | Backend | QA | DevOps | Support |
| --- | --- | --- | --- | --- | --- | --- |
| Release scope | A/R | C | C | C | C | I |
| Code readiness | I | A/R | R | C | C | I |
| Regression approval | I | C | C | A/R | C | I |
| Stage deployment | I | C | I | C | A/R | I |
| Production deployment | I | C | I | C | A/R | I |
| Incident command | I | A/R | R | C | R | C |
| Customer communication | A/R | C | I | I | C | R |
| Backup and restore drill | I | C | I | C | A/R | I |
| Security incident triage | C | A/R | R | C | R | C |

RACI legend: R = responsible, A = accountable, C = consulted, I = informed.

## Versioning

Recommended backend version format:

```text
major.minor.patch-build
```

Example:

```text
1.0.0-rc.1
```

## Monitoring

Minimum metrics:

- application health
- JVM memory and CPU
- request count, latency, and errors
- auth failures
- rate limit denials
- database connection pool
- payroll generation success/failure
- approval action success/failure
- email delivery success/failure

## Logging

Log requirements:

- include request ID
- include tenant code when safe
- include module and workflow name
- do not log secrets, tokens, OTPs, passwords, payroll details, bank details, or sensitive PII
- log high-value business action boundaries

## Alerting

Initial alerts:

- application down
- high 5xx rate
- high auth failure spike
- database connection exhaustion
- Redis unavailable in production
- payroll generation failures
- migration failure
- disk or storage threshold
- backup failure

## Backup And Restore

Backup requirements:

- scheduled MySQL backups
- encrypted backup storage
- restore test at least monthly for production
- backup success alerting
- documented recovery time objective and recovery point objective

Initial targets for pilot:

- RTO: 4 hours
- RPO: 24 hours

These targets should become stricter as customer commitments mature.

## Incident Management

Severity levels:

- SEV1: data breach, cross-tenant exposure, production unavailable, payroll corruption
- SEV2: major workflow outage without broad platform outage
- SEV3: degraded performance or partial module failure
- SEV4: minor issue or support defect

Incident steps:

1. detect and acknowledge
2. assign incident commander
3. stabilize or rollback
4. communicate status
5. preserve evidence
6. resolve
7. run post-incident review
8. create corrective actions

## Operations Runbooks

Required runbooks:

- production deployment
- rollback
- database restore
- failed Flyway migration
- Redis outage
- SMTP outage
- high auth failure spike
- payroll generation failure
- cross-tenant access suspected
- secret rotation
- tenant offboarding

Implemented backup/restore runbook and scripts:

- `docs/runbooks/database-backup-restore-runbook.md`
- `build/release/Backup-MySql.ps1`
- `build/release/Restore-MySql.ps1`

## Support Readiness

Support team needs:

- module ownership map
- known limitations
- customer onboarding checklist
- issue severity guide
- logs and request ID lookup process
- audit event lookup process
- escalation contacts

## Operational Risks

- Missing backup restore evidence creates false production confidence.
- Payroll defects have high business impact.
- Poor tenant isolation testing can create severe security exposure.
- Broad module scope increases release coordination cost.
- Legacy placeholder endpoints can confuse frontend and customers if not retired.
