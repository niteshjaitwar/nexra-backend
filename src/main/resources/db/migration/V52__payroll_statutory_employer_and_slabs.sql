ALTER TABLE payroll_statutory_components ADD COLUMN employer_borne BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE payroll_statutory_components ADD COLUMN wage_ceiling DECIMAL(14,2) NULL;
ALTER TABLE payroll_statutory_components ADD COLUMN min_gross DECIMAL(14,2) NULL;
ALTER TABLE payroll_statutory_components ADD COLUMN max_gross DECIMAL(14,2) NULL;

CREATE TABLE payroll_statutory_slabs (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    country_code VARCHAR(4) NOT NULL,
    component_code VARCHAR(60) NOT NULL,
    min_gross DECIMAL(14,2) NOT NULL,
    max_gross DECIMAL(14,2) NULL,
    fixed_amount DECIMAL(14,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_payroll_statutory_slabs PRIMARY KEY (id)
);

CREATE INDEX ix_payroll_statutory_slabs_lookup
    ON payroll_statutory_slabs(tenant_code, country_code, component_code, active);

-- Apply real statutory wage ceilings and eligibility windows to the India platform pack.
-- PF base is capped at the statutory wage ceiling of 15000.
UPDATE payroll_statutory_components
    SET wage_ceiling = 15000.00
    WHERE id = 'psc-in-pf-platform';

-- ESI applies only when monthly gross is within the eligibility threshold (<= 21000).
UPDATE payroll_statutory_components
    SET max_gross = 21000.00
    WHERE id = 'psc-in-esi-platform';

-- Professional tax becomes a slab-driven component instead of a flat fixed amount.
UPDATE payroll_statutory_components
    SET component_type = 'SLAB', fixed_amount = NULL
    WHERE id = 'psc-in-pt-platform';

-- Employer-borne contributions for the India platform pack.
INSERT INTO payroll_statutory_components (
    id, tenant_code, country_code, component_code, component_name, component_type,
    rate_percent, fixed_amount, employer_borne, wage_ceiling, min_gross, max_gross,
    active, created_at, updated_at, created_by, updated_by, version
) VALUES
(
    'psc-in-pf-employer-platform', '__PLATFORM__', 'IN', 'PF_EMPLOYER', 'Provident Fund (Employer)', 'PERCENTAGE',
    12.0000, NULL, TRUE, 15000.00, NULL, NULL,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
),
(
    'psc-in-esi-employer-platform', '__PLATFORM__', 'IN', 'ESI_EMPLOYER', 'ESI (Employer)', 'PERCENTAGE',
    3.2500, NULL, TRUE, NULL, NULL, 21000.00,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
);

-- Professional tax slabs (representative Karnataka-style bands).
INSERT INTO payroll_statutory_slabs (
    id, tenant_code, country_code, component_code, min_gross, max_gross, fixed_amount,
    active, created_at, updated_at, created_by, updated_by, version
) VALUES
(
    'pts-in-pt-1', '__PLATFORM__', 'IN', 'PROFESSIONAL_TAX', 0.00, 14999.99, 0.00,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
),
(
    'pts-in-pt-2', '__PLATFORM__', 'IN', 'PROFESSIONAL_TAX', 15000.00, NULL, 200.00,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
);
