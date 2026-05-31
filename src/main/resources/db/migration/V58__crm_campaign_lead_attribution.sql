ALTER TABLE crm_leads ADD COLUMN campaign_id VARCHAR(36) NULL;

CREATE INDEX ix_crm_leads_campaign ON crm_leads(tenant_code, campaign_id, domain_updated_at DESC);
