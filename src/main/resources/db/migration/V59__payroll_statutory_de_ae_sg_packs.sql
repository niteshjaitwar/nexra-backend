-- Germany (DE): simplified social-security pack (employee + employer, monthly basis).
-- Pension and health contributions use representative 2026-style rates and ceilings.
INSERT INTO payroll_statutory_components (
    id, tenant_code, country_code, component_code, component_name, component_type,
    rate_percent, fixed_amount, employer_borne, wage_ceiling, wage_floor, min_gross, max_gross,
    active, created_at, updated_at, created_by, updated_by, version
) VALUES
(
    'psc-de-pension-emp-platform', '__PLATFORM__', 'DE', 'PENSION_EMPLOYEE', 'Pension (Employee)', 'PERCENTAGE',
    9.3000, NULL, FALSE, 7550.00, NULL, NULL, NULL,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
),
(
    'psc-de-pension-er-platform', '__PLATFORM__', 'DE', 'PENSION_EMPLOYER', 'Pension (Employer)', 'PERCENTAGE',
    9.3000, NULL, TRUE, 7550.00, NULL, NULL, NULL,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
),
(
    'psc-de-health-emp-platform', '__PLATFORM__', 'DE', 'HEALTH_EMPLOYEE', 'Health Insurance (Employee)', 'PERCENTAGE',
    7.3000, NULL, FALSE, 5512.50, NULL, NULL, NULL,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
),
(
    'psc-de-health-er-platform', '__PLATFORM__', 'DE', 'HEALTH_EMPLOYER', 'Health Insurance (Employer)', 'PERCENTAGE',
    7.3000, NULL, TRUE, 5512.50, NULL, NULL, NULL,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
);

-- United Arab Emirates (AE): employer-only end-of-service / pension-style accrual for platform pack.
INSERT INTO payroll_statutory_components (
    id, tenant_code, country_code, component_code, component_name, component_type,
    rate_percent, fixed_amount, employer_borne, wage_ceiling, wage_floor, min_gross, max_gross,
    active, created_at, updated_at, created_by, updated_by, version
) VALUES
(
    'psc-ae-eos-er-platform', '__PLATFORM__', 'AE', 'EOSB_EMPLOYER', 'End-of-Service (Employer)', 'PERCENTAGE',
    8.3300, NULL, TRUE, NULL, NULL, NULL, NULL,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
);

-- Singapore (SG): simplified CPF ordinary-wage ceiling pack (employee + employer).
INSERT INTO payroll_statutory_components (
    id, tenant_code, country_code, component_code, component_name, component_type,
    rate_percent, fixed_amount, employer_borne, wage_ceiling, wage_floor, min_gross, max_gross,
    active, created_at, updated_at, created_by, updated_by, version
) VALUES
(
    'psc-sg-cpf-emp-platform', '__PLATFORM__', 'SG', 'CPF_EMPLOYEE', 'CPF (Employee)', 'PERCENTAGE',
    20.0000, NULL, FALSE, 7400.00, NULL, NULL, NULL,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
),
(
    'psc-sg-cpf-er-platform', '__PLATFORM__', 'SG', 'CPF_EMPLOYER', 'CPF (Employer)', 'PERCENTAGE',
    17.0000, NULL, TRUE, 7400.00, NULL, NULL, NULL,
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
);
