# API Specification

## API Style

Nexra exposes REST APIs over HTTP. The API contract uses:

- JSON request and response bodies
- common response envelope
- validation errors with structured error payloads
- bearer token authentication for protected endpoints
- tenant context from authenticated token claims
- pagination for list endpoints where applicable
- OpenAPI through Springdoc

Local OpenAPI UI is expected at:

```text
/swagger-ui.html
/swagger-ui/index.html
/v3/api-docs
```

## Common Headers

| Header | Direction | Purpose |
| --- | --- | --- |
| `Authorization: Bearer <token>` | Request | Authenticates protected calls. |
| `X-Request-Id` | Request/Response | Correlates logs, metrics, and support investigations. |
| `Content-Type: application/json` | Request | JSON request body. |
| `Accept: application/json` | Request | JSON response body. |

## Common Response Rules

- Success responses should return the common API response wrapper.
- Validation failures return a structured error response.
- Unauthorized requests return JSON `401`.
- Forbidden requests return JSON `403`.
- Missing records return JSON `404`.
- Business conflicts return JSON `409`.

## Endpoint Inventory

### Auth

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/v1/auth/register` | Register user. |
| POST | `/api/v1/auth/login` | Login and issue tokens. |
| POST | `/api/v1/auth/refresh` | Refresh access token. |
| POST | `/api/v1/auth/logout` | Revoke refresh token/session. |
| POST | `/api/v1/auth/verification/otp/request` | Request OTP verification. |
| POST | `/api/v1/auth/verification/otp/verify` | Verify OTP. |
| POST | `/api/v1/auth/verification/link/request` | Request verification link. |
| POST | `/api/v1/auth/verification/link/verify` | Verify link. |

### Platform And Product Access

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/v1/platform/tenants/provision` | Provision tenant. |
| POST | `/api/v1/tenants` | Create tenant. |
| GET | `/api/v1/admin/users/{userId}/products` | List user product access. |
| POST | `/api/v1/admin/users/{userId}/products` | Grant product access. |
| DELETE | `/api/v1/admin/users/{userId}/products/{product}` | Remove product access. |
| POST | `/api/v1/oauth-clients` | Create OAuth client. |
| GET | `/api/v1/oauth-clients` | List OAuth clients. |

### Employee Core

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/v1/employee-core/status` | Module status. |
| GET | `/api/v1/employee-core/capabilities` | Module capabilities. |
| PUT | `/api/v1/employee-core/organization-profile` | Upsert organization profile. |
| GET | `/api/v1/employee-core/organization-profile` | Get organization profile. |
| POST | `/api/v1/employee-core/departments` | Create department. |
| PUT | `/api/v1/employee-core/departments` | Update department. |
| GET | `/api/v1/employee-core/departments` | List departments. |
| GET | `/api/v1/employee-core/departments/{departmentId}` | Get department. |
| POST | `/api/v1/employee-core/employees` | Create employee. |
| PUT | `/api/v1/employee-core/employees` | Update employee. |
| GET | `/api/v1/employee-core/employees` | List employees. |
| GET | `/api/v1/employee-core/employees/{employeeId}` | Get employee. |
| GET | `/api/v1/employee-core/summary` | Employee summary. |

### Attendance

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/v1/attendance/status` | Module status. |
| GET | `/api/v1/attendance/capabilities` | Module capabilities. |
| PUT | `/api/v1/attendance/shifts` | Upsert shift. |
| GET | `/api/v1/attendance/shifts` | List shifts. |
| POST | `/api/v1/attendance/check-in` | Check in. |
| POST | `/api/v1/attendance/check-out` | Check out. |
| GET | `/api/v1/attendance/records` | List attendance records. |
| GET | `/api/v1/attendance/summary` | Attendance summary. |

### Leave

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/v1/leave/status` | Module status. |
| GET | `/api/v1/leave/capabilities` | Module capabilities. |
| PUT | `/api/v1/leave/leave-types` | Upsert leave type. |
| GET | `/api/v1/leave/leave-types` | List leave types. |
| POST | `/api/v1/leave/holidays` | Create holiday. |
| PUT | `/api/v1/leave/holidays` | Update holiday. |
| GET | `/api/v1/leave/holidays` | List holidays. |
| PUT | `/api/v1/leave/balances` | Adjust balance. |
| GET | `/api/v1/leave/balances` | List balances. |
| POST | `/api/v1/leave/requests` | Create leave request. |
| GET | `/api/v1/leave/requests` | List leave requests. |
| GET | `/api/v1/leave/requests/{requestId}` | Get leave request. |
| POST | `/api/v1/leave/requests/{requestId}/approve` | Approve request. |
| POST | `/api/v1/leave/requests/{requestId}/reject` | Reject request. |

### Payroll

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/v1/payroll/status` | Module status. |
| GET | `/api/v1/payroll/capabilities` | Module capabilities. |
| PUT | `/api/v1/payroll/organization-profile` | Upsert payroll organization profile. |
| GET | `/api/v1/payroll/organization-profile` | Get payroll organization profile. |
| POST | `/api/v1/payroll/organization-profile/logo` | Upload tenant payroll logo (multipart). |
| PUT | `/api/v1/payroll/employees` | Upsert payroll employee profile. |
| POST | `/api/v1/payroll/employees` | Create payroll employee profile. |
| GET | `/api/v1/payroll/employees` | List payroll employee profiles. |
| GET | `/api/v1/payroll/employees/{employeeId}` | Get payroll employee profile. |
| POST | `/api/v1/payroll/generate` | Generate payroll slip. |
| POST | `/api/v1/payroll/generate/from-profile` | Generate payroll from profile. |
| GET | `/api/v1/payroll/{slipId}` | Get payroll slip. |
| GET | `/api/v1/payroll` | List payroll slips. |
| GET | `/api/v1/payroll/payslips/{slipId}/html` | Get payslip HTML. |
| GET | `/api/v1/payroll/payslips/{slipId}/pdf` | Get payslip PDF. |
| GET | `/api/v1/payroll/dependencies/auth` | Check auth dependency. |
| GET | `/api/v1/branding` | Get default public branding metadata. |
| GET | `/api/v1/branding/{tenantCode}` | Get tenant branding metadata with fallback defaults. |
| GET | `/api/v1/branding/assets/{tenantCode}/{filename}` | Fetch uploaded tenant branding logo asset. |

Payroll organization profile now supports optional tenant branding fields used by payslip rendering:

- `brandingLogoPath`
- `brandingCompanyName`
- `brandingWatermarkText`

### Other HRMS Modules

The following modules follow the same pattern: status, capabilities, setup, workflow actions, list/detail, and summary endpoints.

| Module | Base Path |
| --- | --- |
| Timesheet | `/api/v1/timesheet` |
| Onboarding | `/api/v1/onboarding` |
| Performance | `/api/v1/performance` |
| Recruitment | `/api/v1/recruitment` |
| Expense | `/api/v1/expense` |

### CRM

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/v1/crm/leads` | Create lead. |
| GET | `/api/v1/crm/leads/{leadId}` | Get lead. |
| GET | `/api/v1/crm/leads` | List leads. |
| PUT | `/api/v1/crm/leads/{leadId}` | Update lead. |
| DELETE | `/api/v1/crm/leads/{leadId}` | Delete lead. |
| GET | `/api/v1/crm/modules/{moduleKey}/pipeline` | CRM pipeline baseline snapshot. |

## API Versioning

- Stable business APIs should use `/api/v1`.
- Non-versioned endpoints should not be used for launch-safe business APIs.
- Breaking changes require a new version or a migration window.

## Launch API Blockers

| Blocker | Required Resolution | Owner |
| --- | --- | --- |
| Mixed response wrapper implementations | Standardize on the canonical common API response model. | Engineering |
| Missing pagination on list endpoints | Add pagination or document bounded list behavior. | Engineering/Product |
