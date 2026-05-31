CREATE TABLE ops_projects (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(4000) NULL,
    owner_user_id VARCHAR(36) NOT NULL,
    status VARCHAR(40) NOT NULL,
    crm_deal_id VARCHAR(36) NULL,
    department_code VARCHAR(60) NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_projects PRIMARY KEY (id),
    CONSTRAINT uk_ops_projects_tenant_code UNIQUE (tenant_code, code)
);

CREATE TABLE ops_tasks (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    parent_task_id VARCHAR(36) NULL,
    title VARCHAR(240) NOT NULL,
    description VARCHAR(4000) NULL,
    assignee_user_id VARCHAR(36) NULL,
    status VARCHAR(40) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    due_date DATE NULL,
    estimate_hours DECIMAL(10,2) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_tasks PRIMARY KEY (id),
    CONSTRAINT fk_ops_tasks_project FOREIGN KEY (project_id) REFERENCES ops_projects(id)
);

CREATE TABLE ops_approval_requests (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    reference_type VARCHAR(60) NOT NULL,
    reference_id VARCHAR(36) NOT NULL,
    requested_by_user_id VARCHAR(36) NOT NULL,
    approver_user_id VARCHAR(36) NULL,
    status VARCHAR(40) NOT NULL,
    notes VARCHAR(2000) NULL,
    sla_due_at TIMESTAMP NULL,
    decided_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_approval_requests PRIMARY KEY (id)
);

CREATE INDEX ix_ops_tasks_project ON ops_tasks(tenant_code, project_id, status);
CREATE INDEX ix_ops_approval_requests_tenant_status ON ops_approval_requests(tenant_code, status, created_at DESC);
