# Performance And Resilience Validation Runbook

## Purpose

Provide a repeatable execution path and evidence format for launch-gate performance and resilience checks.

## Scope

Applies to:

- API load for auth, HRMS, payroll, and CRM entry points
- long-running soak stability
- dependency degradation handling
- DB pool exhaustion and recovery behavior

## Preconditions

1. Staging environment is deployed with production-like sizing.
2. Observability stack is connected (metrics, logs, traces).
3. Test tenant data set is seeded and stable.
4. Alert routing is enabled for test window.

## Evidence Artifacts

Store all outputs under `build/release/perf-resilience`:

- load summary markdown
- soak summary markdown
- dependency failure summary markdown
- db pool exhaustion summary markdown
- raw command output files
- metric/tracing screenshots or links

## Validation Sequence

1. Baseline health check:
   - verify `/actuator/health/readiness` is `UP`
   - verify no existing high-severity alerts are firing
2. Load test:
   - run target concurrency and throughput test for critical APIs
   - capture p95/p99 latency, error rate, saturation, and CPU/memory
3. Soak test:
   - run sustained medium load for minimum 4 hours
   - verify memory stability and no unbounded error growth
4. Dependency failure tests:
   - simulate redis/mail/storage unavailability one by one
   - verify graceful degradation and clear operational errors
5. DB connection exhaustion:
   - force connection pressure up to pool limit
   - verify timeout behavior, retries, and recovery after pressure removal
6. Post-run verification:
   - health endpoints remain `UP`
   - no data corruption in sampled business workflows

## Gate Pass Criteria

- No unhandled exceptions causing process crash.
- p95/p99 and error-rate thresholds meet launch SLO targets.
- Degradation paths return controlled API errors.
- System recovers without restart after dependency restoration.
- Alerting and traces are sufficient for incident triage.

## Handoff

Attach generated artifact bundle to release evidence:

- `build/release/release-evidence.generated.md`

