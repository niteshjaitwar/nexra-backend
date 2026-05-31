CREATE TABLE at_regularization_requests (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    work_date DATE NOT NULL,
    reason VARCHAR(500),
    requested_check_in_at TIMESTAMP,
    requested_check_out_at TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    approver_email VARCHAR(160),
    decision_comment VARCHAR(500),
    workflow_instance_id VARCHAR(36),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT pk_at_regularization_requests PRIMARY KEY (id)
);

CREATE INDEX ix_at_regularization_tenant_employee ON at_regularization_requests(tenant_code, employee_id);
CREATE INDEX ix_at_regularization_workflow ON at_regularization_requests(workflow_instance_id);

CREATE TABLE user_mfa_recovery_codes (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    code_hash VARCHAR(128) NOT NULL,
    used_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(120) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(120) NOT NULL DEFAULT 'system',
    CONSTRAINT pk_user_mfa_recovery_codes PRIMARY KEY (id)
);

CREATE INDEX ix_user_mfa_recovery_codes_user ON user_mfa_recovery_codes(user_id);
