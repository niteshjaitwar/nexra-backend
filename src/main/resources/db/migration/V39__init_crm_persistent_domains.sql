CREATE TABLE crm_leads (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    full_name VARCHAR(180) NOT NULL,
    email VARCHAR(180) NOT NULL,
    phone VARCHAR(40) NULL,
    company VARCHAR(180) NOT NULL,
    source VARCHAR(80) NULL,
    owner_user_id VARCHAR(36) NOT NULL,
    notes VARCHAR(4000) NULL,
    status VARCHAR(30) NOT NULL,
    domain_created_at TIMESTAMP NOT NULL,
    domain_updated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_crm_leads PRIMARY KEY (id),
    CONSTRAINT uk_crm_leads_tenant_email UNIQUE (tenant_code, email)
);

CREATE TABLE crm_accounts (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    name VARCHAR(180) NOT NULL,
    website VARCHAR(200) NULL,
    industry VARCHAR(80) NULL,
    owner_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_crm_accounts PRIMARY KEY (id)
);

CREATE TABLE crm_contacts (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    account_id VARCHAR(36) NULL,
    full_name VARCHAR(180) NOT NULL,
    email VARCHAR(180) NULL,
    phone VARCHAR(40) NULL,
    owner_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_crm_contacts PRIMARY KEY (id),
    CONSTRAINT fk_crm_contacts_account FOREIGN KEY (account_id) REFERENCES crm_accounts(id)
);

CREATE TABLE crm_deals (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    account_id VARCHAR(36) NULL,
    contact_id VARCHAR(36) NULL,
    title VARCHAR(200) NOT NULL,
    stage VARCHAR(40) NOT NULL,
    value_amount DECIMAL(14,2) NULL,
    currency VARCHAR(10) NULL,
    owner_user_id VARCHAR(36) NOT NULL,
    expected_close_date DATE NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_crm_deals PRIMARY KEY (id),
    CONSTRAINT fk_crm_deals_account FOREIGN KEY (account_id) REFERENCES crm_accounts(id),
    CONSTRAINT fk_crm_deals_contact FOREIGN KEY (contact_id) REFERENCES crm_contacts(id)
);

CREATE TABLE crm_activities (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    lead_id VARCHAR(36) NULL,
    contact_id VARCHAR(36) NULL,
    deal_id VARCHAR(36) NULL,
    activity_type VARCHAR(40) NOT NULL,
    notes VARCHAR(2000) NULL,
    occurred_at TIMESTAMP NOT NULL,
    owner_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_crm_activities PRIMARY KEY (id),
    CONSTRAINT fk_crm_activities_lead FOREIGN KEY (lead_id) REFERENCES crm_leads(id),
    CONSTRAINT fk_crm_activities_contact FOREIGN KEY (contact_id) REFERENCES crm_contacts(id),
    CONSTRAINT fk_crm_activities_deal FOREIGN KEY (deal_id) REFERENCES crm_deals(id)
);

CREATE TABLE crm_tasks (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    lead_id VARCHAR(36) NULL,
    contact_id VARCHAR(36) NULL,
    deal_id VARCHAR(36) NULL,
    title VARCHAR(200) NOT NULL,
    due_date DATE NULL,
    status VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NULL,
    owner_user_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_crm_tasks PRIMARY KEY (id),
    CONSTRAINT fk_crm_tasks_lead FOREIGN KEY (lead_id) REFERENCES crm_leads(id),
    CONSTRAINT fk_crm_tasks_contact FOREIGN KEY (contact_id) REFERENCES crm_contacts(id),
    CONSTRAINT fk_crm_tasks_deal FOREIGN KEY (deal_id) REFERENCES crm_deals(id)
);

CREATE INDEX ix_crm_leads_tenant_owner_status ON crm_leads(tenant_code, owner_user_id, status);
CREATE INDEX ix_crm_contacts_tenant_account ON crm_contacts(tenant_code, account_id);
CREATE INDEX ix_crm_deals_tenant_owner_stage ON crm_deals(tenant_code, owner_user_id, stage);
CREATE INDEX ix_crm_activities_tenant_owner_time ON crm_activities(tenant_code, owner_user_id, occurred_at);
CREATE INDEX ix_crm_tasks_tenant_owner_status_due ON crm_tasks(tenant_code, owner_user_id, status, due_date);
