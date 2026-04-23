ALTER TABLE payroll_slips ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE payroll_slips ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE payroll_slips ADD COLUMN created_by VARCHAR(120) NOT NULL DEFAULT 'system';
ALTER TABLE payroll_slips ADD COLUMN updated_by VARCHAR(120) NOT NULL DEFAULT 'system';
ALTER TABLE payroll_slips ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

UPDATE payroll_slips
SET created_at = COALESCE(created_at, generated_at),
    updated_at = COALESCE(updated_at, generated_at),
    created_by = COALESCE(created_by, generated_by_user_id),
    updated_by = COALESCE(updated_by, generated_by_user_id)
WHERE (created_at = CURRENT_TIMESTAMP OR created_at IS NULL)
   OR (updated_at = CURRENT_TIMESTAMP OR updated_at IS NULL)
   OR created_by = 'system'
   OR updated_by = 'system';
