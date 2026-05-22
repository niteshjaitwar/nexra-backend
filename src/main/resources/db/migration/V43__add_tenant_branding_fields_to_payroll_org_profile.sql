-- Adds tenant-level branding fields for payroll documents.
-- These fields allow tenant-specific logo, display company name, and watermark text.

ALTER TABLE payroll_organization_profiles
    ADD COLUMN branding_logo_path VARCHAR(255) NULL;

ALTER TABLE payroll_organization_profiles
    ADD COLUMN branding_company_name VARCHAR(180) NULL;

ALTER TABLE payroll_organization_profiles
    ADD COLUMN branding_watermark_text VARCHAR(120) NULL;
