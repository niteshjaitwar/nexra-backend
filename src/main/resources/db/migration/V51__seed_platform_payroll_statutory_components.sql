INSERT INTO payroll_statutory_components (
    id, tenant_code, country_code, component_code, component_name, component_type,
    rate_percent, fixed_amount, active, created_at, updated_at, created_by, updated_by, version
) VALUES
(
    'psc-in-pf-platform', '__PLATFORM__', 'IN', 'PF_EMPLOYEE', 'Provident Fund (Employee)', 'PERCENTAGE',
    12.0000, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
),
(
    'psc-in-esi-platform', '__PLATFORM__', 'IN', 'ESI_EMPLOYEE', 'ESI (Employee)', 'PERCENTAGE',
    0.7500, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
),
(
    'psc-in-pt-platform', '__PLATFORM__', 'IN', 'PROFESSIONAL_TAX', 'Professional Tax', 'FIXED',
    NULL, 200.00, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'flyway', 'flyway', 0
);
