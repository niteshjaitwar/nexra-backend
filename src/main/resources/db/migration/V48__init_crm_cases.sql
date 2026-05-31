CREATE TABLE crm_cases (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    subject VARCHAR(240) NOT NULL,
    description VARCHAR(4000) NULL,
    status VARCHAR(40) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    account_id VARCHAR(36) NULL,
    contact_id VARCHAR(36) NULL,
    owner_user_id VARCHAR(36) NOT NULL,
    sla_due_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_crm_cases PRIMARY KEY (id)
);

CREATE INDEX ix_crm_cases_tenant_status ON crm_cases(tenant_code, status, updated_at DESC);
