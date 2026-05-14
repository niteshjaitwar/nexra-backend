# Payroll Module Requirements

Scope: payroll profiles, payroll runs, salary calculation, deductions, statutory compliance, payslips, approvals, and payroll integrations.

## Current State

The module supports payroll organization/employee profiles, payroll generation, payslip HTML/PDF, listing, tenant checks, persistence, and tests.

## Bugs

- `BUG`: Correct stale in-memory storage comments in payroll service and migration notes.
- `BUG`: Prevent duplicate payroll slips/runs for the same employee and pay period unless explicitly rerun with versioning.
- `BUG`: Ensure payroll generation cannot use inactive employee profiles.
- `BUG`: Ensure payslip access is restricted to employee, payroll admin, HR admin, or platform admin.

## Enhancements

- `ENHANCEMENT`: Add payroll run entity separate from individual payslip.
- `ENHANCEMENT`: Add payroll period lock, approval, release, rollback, and correction workflow.
- `ENHANCEMENT`: Add salary components model: earning, deduction, employer contribution, reimbursement, taxable/non-taxable.
- `ENHANCEMENT`: Add salary structure assignment and versioning.
- `ENHANCEMENT`: Add payroll audit ledger.
- `ENHANCEMENT`: Add bank account validation and masked responses.

## Features

- `FEATURE`: Statutory calculation engine per launch geography.
- `FEATURE`: Tax slab/rule configuration.
- `FEATURE`: PF/ESI/PT/TDS or equivalent statutory support per country/state.
- `FEATURE`: Arrears and retroactive salary changes.
- `FEATURE`: Overtime and unpaid leave integration.
- `FEATURE`: Bonus, loan, advance, gratuity, full-and-final settlement.
- `FEATURE`: Bank advice file generation.
- `FEATURE`: Government/statutory filing report exports.
- `FEATURE`: Payslip release and employee acknowledgement.
- `FEATURE`: Payroll variance report.

## Integration Requirements

- `INTEGRATION`: Pull employee master data from employee core.
- `INTEGRATION`: Pull attendance, leave, timesheet, expense, and reimbursement inputs.
- `INTEGRATION`: Integrate with document service for payslips and reports.
- `INTEGRATION`: Integrate with workflow engine for payroll approval.
- `INTEGRATION`: Integrate with audit service for run generation, approval, release, and rollback.
- `INTEGRATION`: Integrate with accounting/export systems later.

## Security Requirements

- `SECURITY`: Treat payroll as high-sensitivity data.
- `SECURITY`: Apply ASVS L3-style controls for payroll actions.
- `SECURITY`: Add field masking for salary, bank, tax, and statutory IDs.
- `SECURITY`: Audit all payroll reads and mutations.
- `SECURITY`: Add segregation of duties: maker/checker/releaser.

## Tests

- `TEST`: Payroll run lifecycle E2E tests.
- `TEST`: Duplicate run, rerun, rollback, release, and locked-period tests.
- `TEST`: Statutory calculation golden-file tests.
- `TEST`: Payslip access-control tests.
- `TEST`: Attendance/leave/expense integration tests.

## Done Criteria

- Payroll can run a complete, auditable, approved pay cycle for the first target geography.
- Every number on a payslip can be traced to source data and calculation rule.

