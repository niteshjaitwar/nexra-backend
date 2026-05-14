# CRM Module Requirements

Scope: leads, accounts, contacts, deals, activities, tasks, pipeline, campaigns, customer support, and CRM automation.

## Current State

The module has persistent CRM lead APIs and a database schema for leads, accounts, contacts, deals, activities, and tasks. Service/controller coverage is currently lead-focused. Product-level CRM mutation endpoints are placeholder-style and must be replaced with real workflows.

## Bugs

- `BUG`: Update misleading comments that describe the lead service as in-memory.
- `BUG`: Ensure lead list sorting is deterministic and indexed.
- `BUG`: Prevent deleting leads that are converted to accounts/deals unless using archival workflow.
- `BUG`: Replace generic `/records/mutate` acceptance with real domain commands or remove before launch.

## Enhancements

- `ENHANCEMENT`: Add lead conversion lifecycle.
- `ENHANCEMENT`: Add lead ownership transfer and assignment rules.
- `ENHANCEMENT`: Add lead source, scoring, status reason, and lost reason.
- `ENHANCEMENT`: Add duplicate detection by email, phone, company, and configurable rules.
- `ENHANCEMENT`: Add CRM search and timeline view model.

## Features

- `FEATURE`: Accounts API.
- `FEATURE`: Contacts API.
- `FEATURE`: Deals API with stage pipeline.
- `FEATURE`: Activities API for calls, emails, meetings, notes.
- `FEATURE`: Tasks/follow-up API.
- `FEATURE`: Lead-to-account/contact/deal conversion.
- `FEATURE`: Sales pipeline forecasting.
- `FEATURE`: Campaigns and marketing lists.
- `FEATURE`: Customer support tickets.
- `FEATURE`: CRM workflow automation rules.
- `FEATURE`: Custom fields and layouts at backend metadata level.
- `FEATURE`: Import/export for leads, accounts, contacts, and deals.

## Integration Requirements

- `INTEGRATION`: Integrate with notification service for task reminders and campaign sends.
- `INTEGRATION`: Integrate with document service for proposals/contracts.
- `INTEGRATION`: Integrate with workflow engine for approvals and automation.
- `INTEGRATION`: Integrate with search service for global CRM search.
- `INTEGRATION`: Integrate with audit service for ownership, stage, value, and deletion changes.
- `INTEGRATION`: Add webhook events for lead, account, contact, deal, task, and ticket changes.

## Security Requirements

- `SECURITY`: Enforce CRM product scope on every endpoint.
- `SECURITY`: Add owner/team/territory-level access controls.
- `SECURITY`: Audit sales value, deal stage, owner transfer, and deletion.
- `SECURITY`: Add export abuse controls and PII masking.

## Tests

- `TEST`: Lead conversion E2E tests.
- `TEST`: Account/contact/deal CRUD and workflow tests.
- `TEST`: Pipeline forecasting tests.
- `TEST`: Owner/team access-control tests.
- `TEST`: Import/export validation tests.

## Done Criteria

- CRM supports a real sales lifecycle from lead capture to deal close, plus activity/task history.
- Placeholder CRM product mutation endpoints are gone or converted into real commands.

