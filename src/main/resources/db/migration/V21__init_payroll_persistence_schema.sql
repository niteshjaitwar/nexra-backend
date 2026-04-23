CREATE TABLE payroll_organization_profiles (
    id VARCHAR(36) PRIMARY KEY,
    tenant_code VARCHAR(60) NOT NULL,
    organization_name VARCHAR(180) NOT NULL,
    legal_entity_name VARCHAR(180) NOT NULL,
    address_line1 VARCHAR(180) NOT NULL,
    address_line2 VARCHAR(180),
    city VARCHAR(120) NOT NULL,
    state VARCHAR(120) NOT NULL,
    country VARCHAR(120) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    default_tax_percent DECIMAL(10, 2) NOT NULL,
    default_pf_percent DECIMAL(10, 2) NOT NULL,
    payroll_contact_email VARCHAR(180),
    payroll_contact_phone VARCHAR(40),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT uk_payroll_org_profiles_tenant UNIQUE (tenant_code)
);

CREATE TABLE payroll_employee_profiles (
    id VARCHAR(36) PRIMARY KEY,
    tenant_code VARCHAR(60) NOT NULL,
    employee_id VARCHAR(64) NOT NULL,
    employee_code VARCHAR(64) NOT NULL,
    employee_name VARCHAR(180) NOT NULL,
    department VARCHAR(120) NOT NULL,
    designation VARCHAR(120) NOT NULL,
    monthly_basic_salary DECIMAL(19, 2) NOT NULL,
    bank_name VARCHAR(120),
    bank_account_masked VARCHAR(80),
    pan_masked VARCHAR(40),
    uan_masked VARCHAR(40),
    email VARCHAR(180),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT uk_payroll_emp_profiles_tenant_employee UNIQUE (tenant_code, employee_id)
);

CREATE INDEX idx_payroll_emp_profiles_tenant_code
    ON payroll_employee_profiles (tenant_code, employee_code);

CREATE TABLE payroll_slips (
    slip_id VARCHAR(36) PRIMARY KEY,
    tenant_code VARCHAR(60) NOT NULL,
    employee_id VARCHAR(64) NOT NULL,
    employee_code VARCHAR(64) NOT NULL,
    employee_name VARCHAR(180) NOT NULL,
    department VARCHAR(120) NOT NULL,
    designation VARCHAR(120) NOT NULL,
    pay_period VARCHAR(20) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    organization_profile_json LONGTEXT NOT NULL,
    employee_profile_json LONGTEXT,
    allowances_json LONGTEXT NOT NULL,
    deductions_json LONGTEXT NOT NULL,
    auth_dependency_status_json LONGTEXT NOT NULL,
    basic_salary DECIMAL(19, 2) NOT NULL,
    tax_percent DECIMAL(10, 2) NOT NULL,
    provident_fund_percent DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(19, 2) NOT NULL,
    provident_fund_amount DECIMAL(19, 2) NOT NULL,
    gross_earnings DECIMAL(19, 2) NOT NULL,
    total_deductions DECIMAL(19, 2) NOT NULL,
    net_pay DECIMAL(19, 2) NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    generated_by_email VARCHAR(180) NOT NULL,
    generated_by_user_id VARCHAR(36) NOT NULL
);

CREATE INDEX idx_payroll_slips_tenant_generated_at
    ON payroll_slips (tenant_code, generated_at DESC);
