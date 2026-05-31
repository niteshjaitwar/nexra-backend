ALTER TABLE workflow_instances ADD COLUMN current_step_index INTEGER NOT NULL DEFAULT 0;
ALTER TABLE workflow_instances ADD COLUMN current_step_name VARCHAR(120) NULL;
ALTER TABLE workflow_instances ADD COLUMN step_status VARCHAR(40) NULL;
ALTER TABLE workflow_instances ADD COLUMN sla_due_at TIMESTAMP NULL;
ALTER TABLE workflow_instances ADD COLUMN escalated BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE workflow_instances ADD COLUMN escalated_at TIMESTAMP NULL;
ALTER TABLE workflow_instances ADD COLUMN completed_at TIMESTAMP NULL;

CREATE INDEX ix_workflow_instances_sla
    ON workflow_instances(status, escalated, sla_due_at);

CREATE TABLE workflow_step_history (
    id VARCHAR(36) NOT NULL,
    instance_id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    step_index INTEGER NOT NULL,
    step_name VARCHAR(120) NOT NULL,
    action VARCHAR(40) NOT NULL,
    actor_email VARCHAR(160) NULL,
    notes VARCHAR(2000) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_workflow_step_history PRIMARY KEY (id),
    CONSTRAINT fk_workflow_step_history_instance FOREIGN KEY (instance_id) REFERENCES workflow_instances(id)
);

CREATE INDEX ix_workflow_step_history_instance
    ON workflow_step_history(instance_id, step_index);
