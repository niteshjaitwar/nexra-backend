# Timesheet Module Requirements

Scope: projects, timesheet entries, submission, approval/rejection, billable hours, cost tracking, and payroll/project integration.

## Current State

The module supports project setup, entries, listing, detail, approval/rejection, summaries, persistence, tenant checks, and tests.

## Bugs

- `BUG`: Prevent duplicate or overlapping time entries where policy disallows them.
- `BUG`: Validate entry dates against employment status, project dates, and locked periods.
- `BUG`: Ensure rejected entries can be corrected without losing audit history.

## Enhancements

- `ENHANCEMENT`: Add weekly/monthly timesheet submission.
- `ENHANCEMENT`: Add project assignment and allocation percentage.
- `ENHANCEMENT`: Add billable/non-billable classification.
- `ENHANCEMENT`: Add client/project/task hierarchy.
- `ENHANCEMENT`: Add timesheet lock and reopen workflow.

## Features

- `FEATURE`: Timesheet policy engine.
- `FEATURE`: Approval routing by project manager and reporting manager.
- `FEATURE`: Cost and utilization reports.
- `FEATURE`: Payroll overtime or variable-pay integration.
- `FEATURE`: Client billing export.

## Integration Requirements

- `INTEGRATION`: Integrate with employee core for active employment and manager.
- `INTEGRATION`: Integrate with operations/projects module when operations is added.
- `INTEGRATION`: Integrate with payroll for overtime or payable hours.
- `INTEGRATION`: Integrate with workflow and notification services.

## Security Requirements

- `SECURITY`: Employees can mutate own draft entries only.
- `SECURITY`: Approvers can act only on assigned scope.
- `SECURITY`: Locked periods require elevated permission.

## Tests

- `TEST`: Overlap, lock, approval, rejection, resubmission, manager scope, and payroll integration tests.
- `TEST`: Weekly submission E2E tests.

## Done Criteria

- Timesheets can support both internal productivity and payable/billable workflows.

