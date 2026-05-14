# Leave Module Requirements

Scope: leave types, holidays, leave balances, requests, approval/rejection, policies, accruals, and payroll impact.

## Current State

The module supports leave types, holidays, balances, requests, approvals, tenant checks, persistence, and tests.

## Bugs

- `BUG`: Prevent leave requests that exceed balance unless policy explicitly allows negative balance.
- `BUG`: Prevent overlapping approved or pending requests.
- `BUG`: Ensure rejected/cancelled requests restore balance correctly.
- `BUG`: Ensure holidays and weekends are handled by policy, not hardcoded assumptions.

## Enhancements

- `ENHANCEMENT`: Add leave policy engine for accrual, carry-forward, encashment, sandwich leave, probation rules, notice-period rules, and document requirements.
- `ENHANCEMENT`: Add cancellation and withdrawal workflow.
- `ENHANCEMENT`: Add manager/team leave calendar APIs.
- `ENHANCEMENT`: Add year-end balance processing.
- `ENHANCEMENT`: Add leave ledger with immutable balance movements.

## Features

- `FEATURE`: Multi-level approval.
- `FEATURE`: Delegation and escalation.
- `FEATURE`: Leave encashment.
- `FEATURE`: Comp-off integration.
- `FEATURE`: Regional holiday calendar per location.
- `FEATURE`: Payroll loss-of-pay integration.

## Integration Requirements

- `INTEGRATION`: Integrate with attendance for absence and approved leave marking.
- `INTEGRATION`: Integrate with payroll for LOP, encashment, and unpaid leave.
- `INTEGRATION`: Integrate with notification service for approvals and reminders.
- `INTEGRATION`: Integrate with workflow engine for approval policies.

## Security Requirements

- `SECURITY`: Employees can create/cancel own requests only.
- `SECURITY`: Managers can approve only scoped employees unless HR/admin role.
- `SECURITY`: Every balance adjustment must be audited.

## Tests

- `TEST`: Accrual, carry-forward, overlap, sandwich leave, cancellation, rejection, approval, LOP, and year-end processing tests.
- `TEST`: Authorization tests for employee, manager, HR, and admin.

## Done Criteria

- Leave balances are ledger-backed and payroll-ready.
- Leave policy is configurable per tenant/location/employment type.

