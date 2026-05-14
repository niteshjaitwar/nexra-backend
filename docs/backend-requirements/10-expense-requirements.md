# Expense Module Requirements

Scope: categories, claims, claim items, approval/rejection, reimbursement, and payroll/accounting integration.

## Current State

The module supports categories, claims, claim items, approval, rejection, reimbursement, persistence, tenant checks, and tests.

## Bugs

- `BUG`: Prevent reimbursement before approval.
- `BUG`: Prevent mutation after reimbursement except through reversal workflow.
- `BUG`: Ensure claim totals match item totals server-side.
- `BUG`: Ensure claim date, currency, and employee references are valid.

## Enhancements

- `ENHANCEMENT`: Add expense policies by category, role, location, amount, and receipt requirement.
- `ENHANCEMENT`: Add receipt document attachments.
- `ENHANCEMENT`: Add multi-currency handling.
- `ENHANCEMENT`: Add advance adjustment.
- `ENHANCEMENT`: Add claim audit ledger.

## Features

- `FEATURE`: Multi-level approval.
- `FEATURE`: Reimbursement batch generation.
- `FEATURE`: Payroll reimbursement posting.
- `FEATURE`: Accounting export.
- `FEATURE`: Fraud/anomaly flags for duplicate receipts, duplicate amounts, and policy violations.

## Integration Requirements

- `INTEGRATION`: Integrate with employee core for active employee and manager.
- `INTEGRATION`: Integrate with document service for receipts.
- `INTEGRATION`: Integrate with workflow engine for approvals.
- `INTEGRATION`: Integrate with payroll for reimbursement.
- `INTEGRATION`: Integrate notification service for claim status.

## Security Requirements

- `SECURITY`: Employees can create own claims only.
- `SECURITY`: Approvers can approve only assigned scope.
- `SECURITY`: Finance can reimburse only approved claims.
- `SECURITY`: Receipts require signed, access-controlled URLs.

## Tests

- `TEST`: Policy validation, approval, rejection, reimbursement, reversal, payroll posting, receipt authorization, and duplicate detection tests.

## Done Criteria

- Expense claims are policy-controlled, auditable, and payroll/accounting-ready.

