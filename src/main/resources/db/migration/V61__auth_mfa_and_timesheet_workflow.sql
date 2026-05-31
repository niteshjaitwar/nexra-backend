ALTER TABLE user_accounts ADD COLUMN mfa_secret VARCHAR(128) NULL;

ALTER TABLE ts_entries ADD COLUMN workflow_instance_id VARCHAR(36) NULL;

CREATE INDEX ix_ts_entries_workflow ON ts_entries(workflow_instance_id);
