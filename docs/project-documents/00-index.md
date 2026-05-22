# Nexra Project Documentation Index

Status: Draft baseline  
Version: 0.2.0  
Last updated: 2026-05-18  
Repository: `nexra-backend`  
Application: Nexra HRMS, Payroll, CRM, and Operations platform  
Architecture: Spring Boot modular monolith  
Audience: founders, product managers, engineers, QA, DevOps, support, and implementation teams

## Governance

Primary terminology:

- Use `Nexra` before generic terms such as platform or application.
- Use `product access` as the primary term for module entitlement.
- Use `/api/v1` for launch-safe APIs. Legacy non-versioned paths are tracked as launch blockers or migration items.
- Use `build -> test -> stage -> release candidate -> production` as the release readiness sequence.
- Use `.\mvnw.cmd --batch-mode verify` as the shared backend quality gate.

## Document Set

| Order | Document | Purpose | Depends On | Owner | Status | Phase | Criticality |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 01 | [Brand Vision And Audience](./01-brand-vision-and-audience.md) | Positioning, audience, brand principles. | None | Product | Draft | Phase 0 | Launch-critical |
| 02 | [Business Requirements Document](./02-business-requirements-document.md) | Business goals, stakeholders, requirements, risks. | 01 | Product | Draft | Phase 0 | Launch-critical |
| 03 | [Product Requirements Document](./03-product-requirements-document.md) | Product modules, personas, acceptance criteria. | 02 | Product | Draft | Phase 0 | Launch-critical |
| 04 | [MVP Scope Document](./04-mvp-scope-document.md) | MVP boundary, launch gates, post-MVP exclusions. | 03 | Product | Draft | Phase 0 | Launch-critical |
| 05 | [User Journeys And Sitemap](./05-user-journeys-and-sitemap.md) | User journeys and product navigation. | 04 | Product/UX | Draft | Phase 1 | Launch-critical |
| 06 | [High Level Design](./06-high-level-design.md) | Architecture, modules, topology, request flow. | 04 | Engineering | Draft | Phase 1 | Launch-critical |
| 07 | [Database Design And ERD](./07-database-design-and-erd.md) | Schema, ERD, indexes, migration order. | 06 | Engineering | Draft | Phase 1 | Launch-critical |
| 08 | [API Specification](./08-api-specification.md) | REST API inventory and contract rules. | 06, 07 | Engineering | Draft | Phase 1 | Launch-critical |
| 09 | [Auth And Security Design](./09-auth-and-security-design.md) | Authentication, authorization, tenant isolation, audit. | 06, 08 | Security/Engineering | Draft | Phase 1 | Launch-critical |
| 10 | [UI/UX Wireframe Document](./10-ui-ux-wireframe-document.md) | Screen structure, states, interaction model. | 05, 08 | UX | Draft | Phase 1 | Launch-critical |
| 11 | [Sprint And Estimation Plan](./11-sprint-and-estimation-plan.md) | Delivery sequence, capacity, dependencies. | 01-10 | Product/Engineering | Draft | Phase 2 | Launch-critical |
| 12 | [Test Strategy](./12-test-strategy.md) | Verification approach and traceability. | 02-11 | QA | Draft | Phase 2 | Launch-critical |
| 13 | [Deployment And Environment Setup](./13-deployment-and-environment-setup.md) | Runtime setup, profiles, deployment steps. | 06, 09, 12 | DevOps | Draft | Phase 2 | Launch-critical |
| 14 | [DevOps Release And Operations](./14-devops-release-and-operations.md) | CI, release, monitoring, RACI, operations. | 11-13 | DevOps/Ops | Draft | Phase 3 | Launch-critical |
| 15 | [Future Scope And Roadmap](./15-future-scope-and-roadmap.md) | Future modules and post-launch roadmap. | 03, 04, 06 | Product/Architecture | Draft | Phase 4 | Post-launch |

## Release Phase Order

- Phase 0: business, product, and MVP documents.
- Phase 1: UX and technical foundation documents.
- Phase 2: delivery, QA, and deployment documents.
- Phase 3: DevOps, release, and operations documents.
- Phase 4: future extension documents.

## Current Product Snapshot

Nexra is a multi-tenant enterprise SaaS platform for HRMS, payroll, CRM, and operational workflows. The current backend is a single Spring Boot application with internal modules for auth, employee core, attendance, leave, timesheet, onboarding, performance, recruitment, expense, payroll, and CRM.

The documentation uses the current implementation as the baseline and separates:

- implemented backend capabilities
- MVP launch scope
- future enterprise scope
- production readiness gates

## Launch-Critical Review Rule

Before production release, every launch-critical document must have:

- named owner
- approved status
- last updated date
- traceability to requirements, tests, or operations where applicable
- no unresolved launch blockers

## Related Existing Documents

- [Backend Market Launch Requirements](../backend-requirements/00-market-launch-requirements.md)
- [Platform Shared Requirements](../backend-requirements/01-platform-shared-requirements.md)
- [Launch Gates](../backend-requirements/14-launch-gates.md)
- [Modular Monolith Architecture](../modular-monolith-architecture.md)
- [Production Delivery Plan](../production-delivery-plan.md)
- [Production Readiness Checklist](../production-readiness-checklist.md)
