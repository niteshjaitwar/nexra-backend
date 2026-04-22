CREATE TABLE at_shifts (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(120) NOT NULL,
    start_time VARCHAR(8) NOT NULL,
    end_time VARCHAR(8) NOT NULL,
    grace_minutes INT NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_at_shifts PRIMARY KEY (id),
    CONSTRAINT uk_at_shifts_tenant_code UNIQUE (tenant_code, code)
);

CREATE TABLE at_records (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    work_date DATE NOT NULL,
    shift_code VARCHAR(40) NULL,
    check_in_at TIMESTAMP NULL,
    check_out_at TIMESTAMP NULL,
    total_hours DECIMAL(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_at_records PRIMARY KEY (id),
    CONSTRAINT uk_at_records_tenant_emp_date UNIQUE (tenant_code, employee_id, work_date)
);

CREATE INDEX idx_at_records_tenant_date ON at_records(tenant_code, work_date);
CREATE INDEX idx_at_records_tenant_employee ON at_records(tenant_code, employee_id);
