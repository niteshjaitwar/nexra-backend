# UI/UX Wireframe Document

## UX Direction

Nexra should feel like a dense, professional operating platform. The UI should prioritize speed, scanability, predictable navigation, and clear workflow status over marketing-style visuals.

Design principles:

- role-specific dashboards
- compact tables and filters
- obvious approval queues
- consistent create/edit/detail patterns
- secure handling of payroll and employee PII
- accessible forms and validation

## Layout Model

```text
┌─────────────────────────────────────────────────────────────┐
│ Top Bar: Tenant, Search, Notifications, User Menu            │
├──────────────┬──────────────────────────────────────────────┤
│ Sidebar      │ Page Header: Title, Status, Actions           │
│              ├──────────────────────────────────────────────┤
│ Dashboard    │ Filters / Summary Metrics                     │
│ HRMS         ├──────────────────────────────────────────────┤
│ Payroll      │ Main Work Area: Table, Form, Detail, Queue     │
│ CRM          │                                              │
│ Operations   │                                              │
│ Admin        ├──────────────────────────────────────────────┤
│ Settings     │ Activity / Audit / Comments Panel, optional   │
└──────────────┴──────────────────────────────────────────────┘
```

## Key Screens

### Login And Verification

```text
┌────────────────────────────┐
│ Nexra                      │
│ Email                      │
│ Password                   │
│ [Login]                    │
│ Forgot password / Verify   │
└────────────────────────────┘
```

Requirements:

- support validation errors
- support OTP or link verification
- avoid revealing whether sensitive accounts exist

### Admin Dashboard

```text
Page: Admin Dashboard
[Tenant Health] [Users] [Products] [Audit Exceptions]

Pending Actions
┌─────────────┬──────────┬───────────┬────────┐
│ Action      │ Module   │ Owner     │ Due    │
├─────────────┼──────────┼───────────┼────────┤
│ Grant HRMS  │ Admin    │ Platform  │ Today  │
└─────────────┴──────────┴───────────┴────────┘
```

### Employee List

```text
Page: Employees
[Search employee] [Department] [Status]                  [+ Employee]

┌──────────────┬──────────────┬────────────┬────────────┬────────┐
│ Employee     │ Code         │ Department │ Work Email │ Status │
├──────────────┼──────────────┼────────────┼────────────┼────────┤
│ Asha Sharma  │ EMP-001      │ Finance    │ ...        │ Active │
└──────────────┴──────────────┴────────────┴────────────┴────────┘
```

### Approval Queue

```text
Page: Approvals
[Leave] [Timesheet] [Expense] [Payroll Exceptions]

┌────────────┬────────────┬────────────┬────────────┬────────────┐
│ Request    │ Employee   │ Amount/Days│ Submitted  │ Actions    │
├────────────┼────────────┼────────────┼────────────┼────────────┤
│ Expense    │ EMP-014    │ 1,250.00   │ 2026-05-17 │ Approve    │
└────────────┴────────────┴────────────┴────────────┴────────────┘
```

### Payroll Run

```text
Page: Payroll Run
[Period] [Department] [Generate]

Summary
Gross Pay | Deductions | Net Pay | Exceptions

Employee Payroll Preview
┌────────────┬────────────┬────────────┬────────────┬────────────┐
│ Employee   │ Gross      │ Deduction  │ Net        │ Status     │
└────────────┴────────────┴────────────┴────────────┴────────────┘
```

### CRM Leads

```text
Page: CRM Leads
[Search] [Owner] [Status] [Source]                       [+ Lead]

┌────────────┬────────────┬────────────┬────────────┬────────────┐
│ Lead       │ Company    │ Owner      │ Status     │ Next Step  │
└────────────┴────────────┴────────────┴────────────┴────────────┘
```

## Component Requirements

- Tables: sorting, filters, pagination, empty state, loading state.
- Forms: inline validation, required markers, save/cancel, dirty state warning.
- Detail pages: overview, activity, audit trail, related records.
- Approvals: approve/reject with comment.
- Payroll: confirmation step before generation.
- Sensitive data: masked by default where appropriate.

## Required Screen States

Every major admin screen must define these states before frontend release:

| Screen Type | Empty State | Loading State | Error State | Bulk Actions |
| --- | --- | --- | --- | --- |
| Employee list | No employees yet; primary action is add employee. | Table skeleton with fixed columns. | Retry and show request ID. | Export, assign department, status change after permission review. |
| Departments | No departments configured. | Compact row skeleton. | Duplicate code and server errors shown inline. | Activate/deactivate where supported. |
| Attendance records | No records for selected date/filter. | Date-filter skeleton. | Check-in/out conflict message. | Export selected records. |
| Leave requests | No pending requests. | Approval queue skeleton. | Balance or date validation message. | Approve/reject selected only after comment policy is defined. |
| Timesheets | No submitted entries. | Weekly grid skeleton. | Project or hour validation error. | Approve/reject selected entries. |
| Expenses | No claims. | Claim list skeleton. | Claim total mismatch or invalid status transition. | Approve/reject/reimburse selected claims with safeguards. |
| Payroll runs | No payroll generated for period. | Payroll preview skeleton. | Payroll generation failure with request ID. | Bulk generate only after preview confirmation. |
| Recruitment candidates | No candidates for selected job. | Pipeline skeleton. | Invalid stage transition message. | Move stage or assign owner where authorized. |
| CRM leads | No leads for filters. | Pipeline/list skeleton. | Duplicate email or ownership error. | Assign owner, update status, export. |
| Product access | No product access granted. | User/product skeleton. | Forbidden or conflicting grant message. | Grant/revoke selected products with confirmation. |

## Interaction Rules

- Bulk actions must show selected count and require confirmation for destructive or financial actions.
- Empty states must provide one primary next action only.
- Error states must show a user-readable message and request ID.
- Loading states must preserve layout dimensions to prevent table jump.
- Unauthorized states must explain access limitation without exposing hidden data.
- Payroll, audit, and employee PII screens must mask sensitive fields unless the user has permission.

## Accessibility Requirements

- Keyboard reachable controls.
- Visible focus states.
- Minimum WCAG AA contrast.
- Form labels tied to inputs.
- Error text associated with fields.
- No color-only status communication.

## Responsive Behavior

- Desktop: sidebar plus table-heavy workflows.
- Tablet: collapsible sidebar and stacked summary cards.
- Mobile: employee self-service first, admin tables become list rows.

## UX Risks

- Too many modules can overwhelm first-time admins.
- Payroll and HR data needs clear permission-based masking.
- Approval screens must expose enough context without forcing page hopping.
