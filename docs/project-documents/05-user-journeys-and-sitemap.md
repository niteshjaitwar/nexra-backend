# User Journeys And Sitemap

## Primary User Journeys

### Tenant Provisioning Journey

1. Platform admin provisions tenant.
2. Tenant admin user is created or invited.
3. Tenant admin verifies account.
4. Tenant admin logs in.
5. Tenant admin configures organization profile.
6. Tenant admin grants product access to users.

Success state: tenant has active users, enabled products, and configured organization data.

### Employee Setup Journey

1. HR admin creates departments.
2. HR admin creates employee records.
3. Employee record is linked to user account when applicable.
4. Employee appears in HR summary and downstream workflows.

Success state: employee can participate in attendance, leave, timesheet, expense, onboarding, performance, and payroll workflows.

### Attendance Journey

1. HR configures shifts.
2. Employee checks in.
3. Employee checks out.
4. Manager or HR reviews records.
5. Attendance summary feeds payroll readiness later.

Success state: one accurate attendance record exists for employee and work date.

### Leave Journey

1. HR configures leave types and holidays.
2. HR adjusts employee leave balance.
3. Employee submits leave request.
4. Manager approves or rejects.
5. Employee sees updated request status.

Success state: leave request is auditable and balance impact is traceable.

### Timesheet Journey

1. Admin creates projects.
2. Employee submits timesheet entry.
3. Manager approves or rejects.
4. Approved entries appear in summaries.

Success state: approved time is available for reporting and future payroll or billing workflows.

### Expense Journey

1. Finance creates categories.
2. Employee submits claim with line items.
3. Manager or finance approves or rejects.
4. Finance reimburses approved claim.

Success state: reimbursement state is controlled and auditable.

### Payroll Journey

1. Payroll admin configures organization payroll profile.
2. Payroll admin configures employee payroll profile.
3. Payroll admin generates payroll.
4. Payroll admin reviews slip.
5. Employee or authorized user retrieves payslip HTML/PDF.

Success state: payroll slip is generated, stored, and accessible only to authorized users.

### Recruitment Journey

1. Recruiter creates job.
2. Recruiter adds candidate.
3. Candidate moves through stages.
4. Stage history is recorded.
5. Recruitment summary reflects pipeline.

Success state: candidate progress is visible and traceable.

### CRM Lead Journey

1. Sales user creates lead.
2. Sales user updates lead status and details.
3. Sales manager views lead list and pipeline.
4. Future workflow converts lead into account, contact, and deal.

Success state: lead is tenant-scoped and owned with clear status.

## Sitemap

The backend repository does not contain the frontend, but the product navigation should map to these product areas.

```text
Nexra
├── Login / Verification
├── Dashboard
├── Admin
│   ├── Tenants
│   ├── Users
│   ├── Product Access
│   ├── OAuth Clients
│   └── Audit Events
├── HRMS
│   ├── Organization Profile
│   ├── Departments
│   ├── Employees
│   ├── Attendance
│   │   ├── Shifts
│   │   ├── Check In / Check Out
│   │   └── Records
│   ├── Leave
│   │   ├── Leave Types
│   │   ├── Holidays
│   │   ├── Balances
│   │   └── Requests
│   ├── Timesheets
│   │   ├── Projects
│   │   └── Entries
│   ├── Onboarding
│   │   ├── Plans
│   │   └── Tasks
│   ├── Performance
│   │   ├── Goals
│   │   └── Reviews
│   ├── Recruitment
│   │   ├── Jobs
│   │   └── Candidates
│   └── Expenses
│       ├── Categories
│       └── Claims
├── Payroll
│   ├── Organization Profile
│   ├── Employee Profiles
│   ├── Payroll Runs
│   └── Payslips
├── CRM
│   ├── Leads
│   ├── Accounts
│   ├── Contacts
│   ├── Deals
│   ├── Activities
│   └── Tasks
└── Operations
    ├── Reports
    ├── Integrations
    ├── Notifications
    ├── Documents
    └── Settings
```

## Navigation Rules

- Users see only modules they are entitled to access.
- Role-specific dashboards should surface pending approvals and exceptions.
- Employee self-service must be separate from admin-heavy configuration screens.
- Payroll, employee PII, audit logs, and admin tools require stricter permissions.
