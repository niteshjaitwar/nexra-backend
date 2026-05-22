# Future Scope And Roadmap

Status: Draft baseline  
Version: 0.1.0  
Last updated: 2026-05-18  
Owner: Product and Architecture  
Criticality: Post-launch

## Purpose

This document consolidates future Nexra scope that appears across the BRD, PRD, MVP, HLD, QA, and operations documents. It keeps post-MVP ambition visible without letting it distort MVP launch commitments.

## Roadmap Principles

- Complete HRMS, payroll, and CRM lead MVP before expanding broad operations scope.
- Add shared platform services before building duplicate workflow logic inside modules.
- Treat compliance-heavy capabilities, especially payroll and documents, as separate release tracks.
- Keep the modular monolith unless scale, team ownership, or deployment independence creates a real need for service extraction.

## Post-MVP Scope Map

| Area | Capability | Why It Matters | Dependency | Target Phase |
| --- | --- | --- | --- | --- |
| Shared platform | Notification service | Email, SMS, and in-app workflow notifications. | Auth, audit | Phase 4 |
| Shared platform | Document service | Secure storage for payslips, employee files, offer letters, invoices. | Security, storage, audit | Phase 4 |
| Shared platform | Workflow engine | Configurable approvals across HRMS, payroll, CRM, and operations. | Audit, product access | Phase 4 |
| Shared platform | Reporting foundation | Dashboards and exports for leadership and managers. | Stable module schemas | Phase 4 |
| Shared platform | Search foundation | Search across employees, candidates, leads, claims, and documents. | Indexing service | Phase 5 |
| CRM | Accounts, contacts, deals | Complete core sales process beyond leads. | CRM lead baseline | Phase 4 |
| CRM | Activities and tasks | Follow-up discipline and customer timeline. | Accounts, contacts, deals | Phase 4 |
| CRM | Campaigns | Marketing outreach and attribution. | Contacts, consent model | Phase 5 |
| CRM | Support desk | Customer issue lifecycle. | Accounts, contacts | Phase 5 |
| Payroll | Statutory rules | Country and state compliance. | Payroll MVP | Phase 4+ |
| Payroll | Bank/payment integrations | Salary and reimbursement payout automation. | Payroll approvals, finance controls | Phase 5 |
| HRMS | Documents | Employee files, onboarding docs, contracts. | Document service | Phase 4 |
| HRMS | Advanced policies | Leave, attendance, shifts, overtime, holiday calendars. | HRMS MVP | Phase 4 |
| Operations | Assets | Company asset assignment and recovery. | Employee core, workflow | Phase 5 |
| Operations | Procurement and vendors | Purchase requests, approvals, vendor records. | Workflow, finance | Phase 5 |
| Operations | Inventory and work orders | Operational stock and service execution. | Procurement, assets | Phase 6 |
| Integrations | Webhooks and events | External system connectivity. | Audit, retry, idempotency | Phase 5 |
| Integrations | Import/export framework | Bulk onboarding and customer migrations. | Validation, jobs | Phase 4 |

## Future Documentation Candidates

- Analytics and reporting specification.
- Workflow engine design.
- Notification service design.
- Document and file storage design.
- Support SOP and customer success playbook.
- Vendor onboarding and procurement BRD.
- Operations module PRD.
- Integration and webhook specification.
- Data retention and privacy compliance policy.

## Post-Launch Entry Criteria

Do not start a future module until:

- MVP release candidate is stable in stage.
- Product owner confirms customer demand or commercial priority.
- Data ownership and tenant model are clear.
- Security and audit impact are understood.
- Engineering estimates include tests, operations, and support readiness.
