# Onboarding Module Requirements

Scope: onboarding plans, tasks, checklists, ownership, status, employee onboarding workflows, and pre-joining/post-joining processes.

## Current State

The module supports plans, tasks, completion, listing, summary, tenant checks, persistence, and tests.

## Bugs

- `BUG`: Prevent tasks from being added to closed or cancelled plans.
- `BUG`: Ensure task completion is authorized by owner or HR/admin.
- `BUG`: Ensure onboarding plan employee references point to valid employee records.

## Enhancements

- `ENHANCEMENT`: Add onboarding templates.
- `ENHANCEMENT`: Add due dates, task dependencies, owners, SLA, and comments.
- `ENHANCEMENT`: Add cancellation and reactivation.
- `ENHANCEMENT`: Add preboarding state before employee active date.
- `ENHANCEMENT`: Add onboarding progress metrics.

## Features

- `FEATURE`: Auto-create onboarding plan from recruitment hired candidate.
- `FEATURE`: Document collection checklist.
- `FEATURE`: IT/admin/HR/facilities task routing.
- `FEATURE`: Employee policy acknowledgements.
- `FEATURE`: Offer-to-joining workflow integration.

## Integration Requirements

- `INTEGRATION`: Integrate with recruitment when candidate is hired.
- `INTEGRATION`: Integrate with employee core to activate employee after required tasks.
- `INTEGRATION`: Integrate with document service for collected documents.
- `INTEGRATION`: Integrate with notification and workflow services.

## Security Requirements

- `SECURITY`: Restrict pre-hire candidate/employee sensitive data.
- `SECURITY`: Audit task completion and document access.

## Tests

- `TEST`: Template creation, plan generation, task dependency, completion authorization, document checklist, and employee activation tests.

## Done Criteria

- Onboarding can run a complete joining workflow from offer accepted to employee activation.

