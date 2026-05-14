# Attendance Module Requirements

Scope: shifts, attendance records, check-in, check-out, summaries, attendance rules, and attendance integrations.

## Current State

The module supports shift setup, check-in/check-out, attendance records, summaries, tenant checks, persistence, and tests.

## Bugs

- `BUG`: Prevent duplicate check-ins for the same employee/work date/shift unless policy permits split shifts.
- `BUG`: Handle missing checkout, overnight shifts, timezone boundaries, and daylight saving edge cases.
- `BUG`: Ensure manual corrections require authorization and audit.

## Enhancements

- `ENHANCEMENT`: Add shift templates, weekly rosters, holidays, grace periods, late/early markers, and break rules.
- `ENHANCEMENT`: Add attendance regularization workflow.
- `ENHANCEMENT`: Add manager approval for corrections.
- `ENHANCEMENT`: Add monthly attendance summary per employee.
- `ENHANCEMENT`: Add exportable attendance register.

## Features

- `FEATURE`: Biometric/device integration ingestion API.
- `FEATURE`: Geo-fenced/mobile attendance support at API level.
- `FEATURE`: Overtime calculation.
- `FEATURE`: Comp-off generation from overtime or holiday work.
- `FEATURE`: Absence detection and notification.
- `FEATURE`: Payroll-ready attendance lock period.

## Integration Requirements

- `INTEGRATION`: Integrate with leave module to mark approved leave.
- `INTEGRATION`: Integrate with payroll for unpaid days, overtime, late penalties, and attendance lock.
- `INTEGRATION`: Integrate with notification service for missing checkout and regularization reminders.
- `INTEGRATION`: Integrate audit service for corrections and imports.

## Security Requirements

- `SECURITY`: Prevent employee from mutating final attendance except via request workflow.
- `SECURITY`: Enforce manager and HR scopes.
- `SECURITY`: Validate device/API ingestion source using signed credentials.

## Tests

- `TEST`: Overnight shift, duplicate check-in, missing checkout, correction approval, leave overlap, and payroll lock tests.
- `TEST`: Device ingestion idempotency tests.
- `TEST`: Timezone and month-end tests.

## Done Criteria

- Attendance can feed payroll reliably and withstand audit.
- Corrections and imports are traceable and permission-controlled.

