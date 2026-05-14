-- V42: Link employee records to their UserAccount by adding user_account_id.
-- This fixes the critical security bug where requestingOwnEmployee() compared
-- employeeId (business domain UUID) against userId (auth domain UUID) — they
-- are different namespaces, so the comparison always returned false, breaking
-- employee self-service access control in Leave, Attendance, Timesheet, and Expense.
--
-- After this migration:
--   1. When creating/updating an employee, pass the associated userAccountId.
--   2. requestingOwnEmployee() compares actor.userId() against employee.user_account_id.

ALTER TABLE ec_employees
    ADD COLUMN user_account_id VARCHAR(36) NULL COMMENT 'References users.userId in auth module — links HR profile to login identity' AFTER uan_masked;

CREATE INDEX idx_ec_employees_user_account_id ON ec_employees (user_account_id);
