CREATE TABLE lv_leave_types (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(120) NOT NULL,
    paid BOOLEAN NOT NULL,
    default_annual_quota DECIMAL(10,2) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_lv_leave_types PRIMARY KEY (id),
    CONSTRAINT uk_lv_leave_types_tenant_code UNIQUE (tenant_code, code)
);

CREATE TABLE lv_holidays (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    holiday_date DATE NOT NULL,
    name VARCHAR(160) NOT NULL,
    location_code VARCHAR(40) NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_lv_holidays PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_lv_holidays_tenant_date_location
    ON lv_holidays(tenant_code, holiday_date, location_code);

CREATE TABLE lv_balances (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    leave_type_code VARCHAR(40) NOT NULL,
    opening_balance DECIMAL(10,2) NOT NULL,
    accrued_balance DECIMAL(10,2) NOT NULL,
    used_balance DECIMAL(10,2) NOT NULL,
    adjusted_balance DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_lv_balances PRIMARY KEY (id),
    CONSTRAINT uk_lv_balances_tenant_emp_type UNIQUE (tenant_code, employee_id, leave_type_code)
);

CREATE TABLE lv_leave_requests (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    leave_type_code VARCHAR(40) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days DECIMAL(10,2) NOT NULL,
    reason VARCHAR(500) NULL,
    status VARCHAR(30) NOT NULL,
    approver_user_id VARCHAR(36) NULL,
    approver_email VARCHAR(160) NULL,
    decision_comment VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_lv_leave_requests PRIMARY KEY (id)
);

CREATE INDEX idx_lv_leave_types_tenant ON lv_leave_types(tenant_code);
CREATE INDEX idx_lv_balances_tenant_employee ON lv_balances(tenant_code, employee_id);
CREATE INDEX idx_lv_requests_tenant_employee ON lv_leave_requests(tenant_code, employee_id);
CREATE INDEX idx_lv_requests_tenant_status ON lv_leave_requests(tenant_code, status);
