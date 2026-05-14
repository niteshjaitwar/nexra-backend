# Recruitment Module Requirements

Scope: jobs, candidates, stage changes, candidate history, hiring workflow, and offer handoff.

## Current State

The module supports jobs, candidates, stage changes, candidate history, summaries, persistence, tenant checks, and tests.

## Bugs

- `BUG`: Prevent stage transitions that skip mandatory stages unless policy allows it.
- `BUG`: Prevent duplicate candidates by tenant/email/phone where configured.
- `BUG`: Ensure candidate history is append-only.

## Enhancements

- `ENHANCEMENT`: Add job approval workflow.
- `ENHANCEMENT`: Add interview rounds, interviewers, feedback, and scorecards.
- `ENHANCEMENT`: Add candidate source tracking and recruiter ownership.
- `ENHANCEMENT`: Add resume/document attachments.
- `ENHANCEMENT`: Add candidate search and filters.

## Features

- `FEATURE`: Offer letter generation.
- `FEATURE`: Offer approval and acceptance tracking.
- `FEATURE`: Candidate portal API foundation.
- `FEATURE`: Referral workflow.
- `FEATURE`: Recruitment analytics: funnel, source quality, time-to-hire, offer acceptance.
- `FEATURE`: Convert hired candidate to employee and onboarding plan.

## Integration Requirements

- `INTEGRATION`: Integrate with document service for resumes and offer letters.
- `INTEGRATION`: Integrate with onboarding after hire.
- `INTEGRATION`: Integrate with employee core after offer acceptance.
- `INTEGRATION`: Integrate notification service for interview, offer, and rejection communication.

## Security Requirements

- `SECURITY`: Restrict candidate PII to authorized recruiters/interviewers.
- `SECURITY`: Audit every stage change, feedback submission, and offer action.

## Tests

- `TEST`: Stage transition, duplicate detection, interview, offer, hire-to-onboarding, and authorization tests.

## Done Criteria

- Recruitment can manage requisition to hire without manual backend intervention.

