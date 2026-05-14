# Performance Module Requirements

Scope: goals, reviews, review cycles, manager feedback, scoring, calibration, and performance history.

## Current State

The module supports goals, reviews, completion, summaries, persistence, tenant checks, and tests.

## Bugs

- `BUG`: Prevent review completion without required ratings/comments.
- `BUG`: Prevent unauthorized users from viewing private manager comments.
- `BUG`: Ensure goals and reviews are tied to valid active employees.

## Enhancements

- `ENHANCEMENT`: Add review cycles and templates.
- `ENHANCEMENT`: Add goal progress updates and check-ins.
- `ENHANCEMENT`: Add manager, self, peer, and HR review sections.
- `ENHANCEMENT`: Add calibration status and rating distribution.
- `ENHANCEMENT`: Add performance history per employee.

## Features

- `FEATURE`: OKR/KRA support.
- `FEATURE`: 360 feedback.
- `FEATURE`: Performance improvement plan.
- `FEATURE`: Promotion/compensation recommendation workflow.
- `FEATURE`: Skill matrix integration.

## Integration Requirements

- `INTEGRATION`: Integrate with employee core for hierarchy and employment status.
- `INTEGRATION`: Integrate with workflow engine for review routing and calibration.
- `INTEGRATION`: Integrate with notification service for cycle reminders.
- `INTEGRATION`: Integrate with payroll/compensation later for merit changes.

## Security Requirements

- `SECURITY`: Field-level access for self-review, manager-review, HR calibration, and final rating.
- `SECURITY`: Audit rating changes and review reopen events.

## Tests

- `TEST`: Review cycle, goal update, manager scope, calibration, completion, reopen, and privacy tests.

## Done Criteria

- Performance records are secure, auditable, and usable for employee lifecycle decisions.

