CREATE TABLE payroll_statutory_components (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    country_code VARCHAR(4) NOT NULL,
    component_code VARCHAR(60) NOT NULL,
    component_name VARCHAR(160) NOT NULL,
    component_type VARCHAR(20) NOT NULL,
    rate_percent DECIMAL(8,4) NULL,
    fixed_amount DECIMAL(14,2) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_payroll_statutory_components PRIMARY KEY (id),
    CONSTRAINT uk_payroll_statutory_components UNIQUE (tenant_code, country_code, component_code)
);
