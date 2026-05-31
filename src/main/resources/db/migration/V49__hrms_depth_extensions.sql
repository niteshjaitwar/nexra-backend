CREATE TABLE employee_positions (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    title VARCHAR(160) NOT NULL,
    department_code VARCHAR(60) NULL,
    manager_employee_id VARCHAR(36) NULL,
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_employee_positions PRIMARY KEY (id)
);

CREATE TABLE recruitment_offers (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    candidate_id VARCHAR(36) NOT NULL,
    job_id VARCHAR(36) NOT NULL,
    offer_status VARCHAR(40) NOT NULL,
    compensation_amount DECIMAL(14,2) NULL,
    currency VARCHAR(10) NULL,
    valid_until DATE NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_recruitment_offers PRIMARY KEY (id)
);

CREATE TABLE leave_accrual_policies (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    leave_type_code VARCHAR(60) NOT NULL,
    accrual_rate_days DECIMAL(8,2) NOT NULL,
    carry_forward_days DECIMAL(8,2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_leave_accrual_policies PRIMARY KEY (id),
    CONSTRAINT uk_leave_accrual_policies UNIQUE (tenant_code, leave_type_code)
);
