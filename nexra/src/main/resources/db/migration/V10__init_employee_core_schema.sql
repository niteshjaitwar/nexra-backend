CREATE TABLE ec_organization_profiles (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    organization_name VARCHAR(160) NOT NULL,
    legal_entity_name VARCHAR(160) NOT NULL,
    address_line1 VARCHAR(200) NOT NULL,
    address_line2 VARCHAR(200) NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(30) NOT NULL,
    currency VARCHAR(12) NOT NULL,
    default_tax_percent DECIMAL(10,2) NOT NULL,
    default_provident_fund_percent DECIMAL(10,2) NOT NULL,
    payroll_contact_email VARCHAR(160) NULL,
    payroll_contact_phone VARCHAR(40) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_ec_organization_profiles PRIMARY KEY (id),
    CONSTRAINT uk_ec_org_profiles_tenant UNIQUE (tenant_code)
);

CREATE TABLE ec_departments (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(120) NOT NULL,
    manager_employee_id VARCHAR(36) NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_ec_departments PRIMARY KEY (id),
    CONSTRAINT uk_ec_departments_tenant_code UNIQUE (tenant_code, code)
);

CREATE TABLE ec_employees (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    employee_code VARCHAR(60) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    work_email VARCHAR(160) NOT NULL,
    department_id VARCHAR(36) NULL,
    designation VARCHAR(120) NOT NULL,
    status VARCHAR(40) NOT NULL,
    join_date DATE NOT NULL,
    monthly_basic_salary DECIMAL(14,2) NOT NULL,
    bank_name VARCHAR(120) NULL,
    bank_account_masked VARCHAR(50) NULL,
    pan_masked VARCHAR(30) NULL,
    uan_masked VARCHAR(30) NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_ec_employees PRIMARY KEY (id),
    CONSTRAINT fk_ec_employees_department FOREIGN KEY (department_id) REFERENCES ec_departments(id),
    CONSTRAINT uk_ec_employees_tenant_empcode UNIQUE (tenant_code, employee_code),
    CONSTRAINT uk_ec_employees_tenant_email UNIQUE (tenant_code, work_email)
);

CREATE INDEX idx_ec_departments_tenant ON ec_departments(tenant_code);
CREATE INDEX idx_ec_employees_tenant ON ec_employees(tenant_code);
CREATE INDEX idx_ec_employees_tenant_department ON ec_employees(tenant_code, department_id);
