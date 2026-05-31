CREATE TABLE workflow_definitions (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    product_code VARCHAR(40) NOT NULL,
    module_key VARCHAR(80) NOT NULL,
    name VARCHAR(160) NOT NULL,
    trigger_event VARCHAR(80) NOT NULL,
    criteria_json TEXT NOT NULL,
    actions_json TEXT NOT NULL,
    priority INTEGER NOT NULL DEFAULT 100,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_workflow_definitions PRIMARY KEY (id)
);

CREATE TABLE workflow_instances (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    product_code VARCHAR(40) NOT NULL,
    module_key VARCHAR(80) NOT NULL,
    definition_id VARCHAR(36) NULL,
    trigger_event VARCHAR(80) NOT NULL,
    status VARCHAR(40) NOT NULL,
    actor_email VARCHAR(160) NULL,
    payload_json TEXT NULL,
    result_json TEXT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_workflow_instances PRIMARY KEY (id)
);

CREATE INDEX ix_workflow_instances_tenant_module ON workflow_instances(tenant_code, module_key, created_at DESC);
