# Operations Suite Requirements

Scope: backend modules needed for organization operations beyond HRMS, payroll, and CRM.

## Current State

The current backend does not yet contain a complete operations suite. Timesheet and expense provide small operational slices, but operations as a product needs separate domain modules.

## Required Modules

- `FEATURE`: Projects and task management.
- `FEATURE`: Internal service desk/tickets.
- `FEATURE`: Asset management.
- `FEATURE`: Inventory.
- `FEATURE`: Procurement.
- `FEATURE`: Vendors.
- `FEATURE`: Contracts.
- `FEATURE`: Work orders.
- `FEATURE`: Approval policies.
- `FEATURE`: Operational reporting.

## Projects And Tasks

- `FEATURE`: Project, milestone, task, subtask, assignment, priority, due date, dependency, status, comments.
- `INTEGRATION`: Integrate with timesheet for project time.
- `INTEGRATION`: Integrate with employee core for assignment and manager scope.
- `TEST`: Project lifecycle, assignment, status, and authorization tests.

## Service Desk

- `FEATURE`: Ticket categories, priorities, SLAs, assignment, comments, attachments, resolution, reopen.
- `INTEGRATION`: Integrate with notification, document, workflow, audit, and employee core.
- `TEST`: Ticket lifecycle, SLA, escalation, and permission tests.

## Assets

- `FEATURE`: Asset catalog, assignment, return, repair, loss, depreciation metadata, audit.
- `INTEGRATION`: Integrate with employee lifecycle and onboarding/offboarding.
- `TEST`: Assignment, transfer, return, and offboarding tests.

## Procurement And Vendors

- `FEATURE`: Vendor master, purchase requests, purchase orders, goods receipt, invoice matching.
- `INTEGRATION`: Integrate with workflow approval, document storage, and audit.
- `TEST`: PR-to-PO lifecycle, approval, rejection, and vendor authorization tests.

## Inventory

- `FEATURE`: Items, locations, stock ledger, stock movement, reorder levels.
- `INTEGRATION`: Integrate with procurement and assets.
- `TEST`: Stock ledger, movement, adjustment, and reconciliation tests.

## Contracts

- `FEATURE`: Contract metadata, parties, dates, renewal reminders, attachments, approval, status.
- `INTEGRATION`: Integrate with vendors, CRM accounts, document service, notification, and audit.
- `TEST`: Renewal reminder, approval, expiry, and access-control tests.

## Security Requirements

- `SECURITY`: Role and scope model for operations admin, project manager, asset manager, procurement manager, finance, and requester.
- `SECURITY`: Audit all asset, procurement, inventory, contract, and ticket state changes.
- `SECURITY`: Protect financial and vendor data with field-level controls.

## Done Criteria

- Operations suite supports the first launch vertical with real workflow-backed modules, not generic mutation endpoints.

