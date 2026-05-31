ALTER TABLE lv_leave_requests ADD COLUMN workflow_instance_id VARCHAR(36) NULL;

CREATE INDEX ix_lv_leave_requests_workflow ON lv_leave_requests(workflow_instance_id);

CREATE UNIQUE INDEX uq_ops_projects_crm_deal ON ops_projects(tenant_code, crm_deal_id);
