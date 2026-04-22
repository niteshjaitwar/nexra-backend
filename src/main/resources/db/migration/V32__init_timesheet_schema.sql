CREATE TABLE ts_projects (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    project_code VARCHAR(60) NOT NULL,
    project_name VARCHAR(160) NOT NULL,
    client_name VARCHAR(160) NULL,
    billable BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_ts_projects PRIMARY KEY (id),
    CONSTRAINT uk_ts_projects_tenant_code UNIQUE (tenant_code, project_code)
);

CREATE TABLE ts_entries (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    work_date DATE NOT NULL,
    project_code VARCHAR(60) NOT NULL,
    task_name VARCHAR(160) NOT NULL,
    hours DECIMAL(10,2) NOT NULL,
    billable BOOLEAN NOT NULL,
    status VARCHAR(30) NOT NULL,
    approver_user_id VARCHAR(36) NULL,
    approver_email VARCHAR(160) NULL,
    approval_comment VARCHAR(500) NULL,
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_ts_entries PRIMARY KEY (id)
);

CREATE INDEX idx_ts_entries_tenant_employee_date ON ts_entries(tenant_code, employee_id, work_date);
CREATE INDEX idx_ts_entries_tenant_status ON ts_entries(tenant_code, status);
