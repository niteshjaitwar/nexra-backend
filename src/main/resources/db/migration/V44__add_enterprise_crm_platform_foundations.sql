CREATE TABLE crm_custom_field_definitions (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    module_key VARCHAR(60) NOT NULL,
    field_key VARCHAR(80) NOT NULL,
    label VARCHAR(120) NOT NULL,
    field_type VARCHAR(40) NOT NULL,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    searchable BOOLEAN NOT NULL DEFAULT FALSE,
    options_json TEXT NULL,
    validation_json TEXT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_crm_custom_field_definitions PRIMARY KEY (id),
    CONSTRAINT uk_crm_custom_field_definitions_key UNIQUE (tenant_code, module_key, field_key)
);

CREATE TABLE crm_custom_field_values (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    module_key VARCHAR(60) NOT NULL,
    record_id VARCHAR(36) NOT NULL,
    field_key VARCHAR(80) NOT NULL,
    value_text TEXT NULL,
    value_number DECIMAL(19,4) NULL,
    value_date DATE NULL,
    value_datetime TIMESTAMP NULL,
    value_boolean BOOLEAN NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_crm_custom_field_values PRIMARY KEY (id),
    CONSTRAINT uk_crm_custom_field_values_key UNIQUE (tenant_code, module_key, record_id, field_key)
);

CREATE TABLE crm_workflow_rules (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    module_key VARCHAR(60) NOT NULL,
    name VARCHAR(160) NOT NULL,
    trigger_event VARCHAR(60) NOT NULL,
    criteria_json TEXT NOT NULL,
    actions_json TEXT NOT NULL,
    priority INTEGER NOT NULL DEFAULT 100,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_crm_workflow_rules PRIMARY KEY (id)
);

CREATE TABLE crm_record_sharing_rules (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    module_key VARCHAR(60) NOT NULL,
    name VARCHAR(160) NOT NULL,
    criteria_json TEXT NOT NULL,
    principal_type VARCHAR(40) NOT NULL,
    principal_key VARCHAR(120) NOT NULL,
    access_level VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_crm_record_sharing_rules PRIMARY KEY (id)
);

CREATE TABLE integration_webhook_subscriptions (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    product_key VARCHAR(40) NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    target_url VARCHAR(500) NOT NULL,
    secret_hash VARCHAR(128) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    failure_count INTEGER NOT NULL DEFAULT 0,
    last_success_at TIMESTAMP NULL,
    last_failure_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_integration_webhook_subscriptions PRIMARY KEY (id)
);

CREATE INDEX ix_crm_custom_fields_tenant_module ON crm_custom_field_definitions(tenant_code, module_key, active);
CREATE INDEX ix_crm_custom_values_record ON crm_custom_field_values(tenant_code, module_key, record_id);
CREATE INDEX ix_crm_workflow_rules_trigger ON crm_workflow_rules(tenant_code, module_key, trigger_event, active, priority);
CREATE INDEX ix_crm_record_sharing_rules_module ON crm_record_sharing_rules(tenant_code, module_key, active);
CREATE INDEX ix_integration_webhooks_event ON integration_webhook_subscriptions(tenant_code, product_key, event_type, active);
