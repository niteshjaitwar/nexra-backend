CREATE TABLE crm_campaigns (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    name VARCHAR(240) NOT NULL,
    campaign_type VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    description VARCHAR(4000) NULL,
    budget DECIMAL(16,2) NULL,
    actual_cost DECIMAL(16,2) NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    owner_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_crm_campaigns PRIMARY KEY (id)
);

CREATE INDEX ix_crm_campaigns_tenant_status ON crm_campaigns(tenant_code, status, updated_at DESC);
