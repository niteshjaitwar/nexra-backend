-- Earnings-band floor: contributions apply only to gross above this threshold
-- (e.g. UK National Insurance primary/secondary thresholds).
ALTER TABLE payroll_statutory_components ADD COLUMN wage_floor DECIMAL(14,2) NULL;

-- =====================================================================
-- United States (US) platform statutory pack: FICA (monthly basis).
-- Social Security wage base 168,600/yr -> 14,050.00 monthly ceiling.
-- =====================================================================
INSERT INTO payroll_statutory_components (
    id, tenant_code, country_code, component_code, component_name, component_type,
    rate_percent, fixed_amount, employer_borne, wage_ceiling, wage_floor, min_gross, max_gross,
    active, created_at, updated_at, created_by, updated_by, version
) VALUES
(
    'psc-us-ss-emp-platform', '__PLATFORM__', 'US', 'SOCIAL_SECURITY', 'Social Security (Employee)', 'PERCENTAGE',
    6.2000, NULL, FALSE, 14050.00, NULL, NULL, NULL,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
),
(
    'psc-us-ss-er-platform', '__PLATFORM__', 'US', 'SOCIAL_SECURITY_EMPLOYER', 'Social Security (Employer)', 'PERCENTAGE',
    6.2000, NULL, TRUE, 14050.00, NULL, NULL, NULL,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
),
(
    'psc-us-med-emp-platform', '__PLATFORM__', 'US', 'MEDICARE', 'Medicare (Employee)', 'PERCENTAGE',
    1.4500, NULL, FALSE, NULL, NULL, NULL, NULL,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
),
(
    'psc-us-med-er-platform', '__PLATFORM__', 'US', 'MEDICARE_EMPLOYER', 'Medicare (Employer)', 'PERCENTAGE',
    1.4500, NULL, TRUE, NULL, NULL, NULL, NULL,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
);

-- =====================================================================
-- United Kingdom (GB) platform statutory pack: National Insurance Class 1.
-- Earnings band: floor = primary/secondary threshold, ceiling = upper limit.
-- =====================================================================
INSERT INTO payroll_statutory_components (
    id, tenant_code, country_code, component_code, component_name, component_type,
    rate_percent, fixed_amount, employer_borne, wage_ceiling, wage_floor, min_gross, max_gross,
    active, created_at, updated_at, created_by, updated_by, version
) VALUES
(
    'psc-gb-ni-emp-platform', '__PLATFORM__', 'GB', 'NI_EMPLOYEE', 'National Insurance (Employee)', 'PERCENTAGE',
    8.0000, NULL, FALSE, 4189.00, 1048.00, NULL, NULL,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
),
(
    'psc-gb-ni-er-platform', '__PLATFORM__', 'GB', 'NI_EMPLOYER', 'National Insurance (Employer)', 'PERCENTAGE',
    13.8000, NULL, TRUE, NULL, 758.00, NULL, NULL,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
);

-- =====================================================================
-- Statutory filing artifacts: a generated, immutable summary of statutory
-- liabilities for a tenant/country/period (e.g. PF ECR, Form 941, RTI FPS).
-- =====================================================================
CREATE TABLE payroll_statutory_filings (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    country_code VARCHAR(4) NOT NULL,
    period VARCHAR(7) NOT NULL,
    filing_type VARCHAR(60) NOT NULL,
    reference_number VARCHAR(80) NOT NULL,
    status VARCHAR(30) NOT NULL,
    employee_count INTEGER NOT NULL,
    total_gross DECIMAL(16,2) NOT NULL,
    employee_contribution DECIMAL(16,2) NOT NULL,
    employer_contribution DECIMAL(16,2) NOT NULL,
    total_contribution DECIMAL(16,2) NOT NULL,
    component_totals_json TEXT NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_payroll_statutory_filings PRIMARY KEY (id),
    CONSTRAINT uq_payroll_filing_period UNIQUE (tenant_code, country_code, period, filing_type)
);

CREATE INDEX ix_payroll_filings_tenant
    ON payroll_statutory_filings(tenant_code, country_code, period);
