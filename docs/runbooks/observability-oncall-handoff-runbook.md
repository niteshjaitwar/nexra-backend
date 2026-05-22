# Observability And On-Call Handoff Runbook

## Purpose

Define minimum observability and on-call routing evidence required before release sign-off.

## Required Dashboards

1. Platform API dashboard:
   - request rate
   - p95/p99 latency
   - error rate by module
2. Security/auth dashboard:
   - login failures
   - token refresh failures
   - rate-limit rejections
3. Data/runtime dashboard:
   - DB pool usage
   - JVM memory and GC
   - thread pool saturation
4. Business operations dashboard:
   - payroll run status
   - expense approval backlog
   - recruitment/onboarding throughput

## Required Alerts

- `SEV-1`: service unavailable, sustained 5xx spike
- `SEV-2`: auth failure surge, DB pool saturation, queue/job failures
- `SEV-3`: elevated latency, workflow backlog growth

Each alert must have:

- owner team
- pager route
- runbook URL

## Tracing Requirements

Cross-module traces must exist for:

- auth login + tenant access resolution
- employee to attendance/timesheet flows
- payroll generation and payslip retrieval
- CRM lead lifecycle actions

Trace data must include request ID and tenant tag when safe.

## On-Call Route Validation

1. Trigger one synthetic `SEV-3` alert in staging.
2. Confirm delivery to configured destination.
3. Acknowledge and close the alert.
4. Record timestamp and responder details in release evidence.

## Evidence Capture

Attach the following to release evidence:

- dashboard URLs
- alert policy references
- trace query references
- on-call synthetic alert check output

