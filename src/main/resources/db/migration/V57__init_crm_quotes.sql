CREATE TABLE crm_quotes (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    quote_number VARCHAR(60) NOT NULL,
    title VARCHAR(240) NOT NULL,
    status VARCHAR(40) NOT NULL,
    currency VARCHAR(4) NOT NULL,
    deal_id VARCHAR(36) NULL,
    account_id VARCHAR(36) NULL,
    contact_id VARCHAR(36) NULL,
    owner_user_id VARCHAR(36) NOT NULL,
    subtotal DECIMAL(16,2) NOT NULL,
    discount_total DECIMAL(16,2) NOT NULL,
    tax_total DECIMAL(16,2) NOT NULL,
    grand_total DECIMAL(16,2) NOT NULL,
    valid_until DATE NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_crm_quotes PRIMARY KEY (id),
    CONSTRAINT uq_crm_quotes_number UNIQUE (tenant_code, quote_number)
);

CREATE INDEX ix_crm_quotes_tenant_status ON crm_quotes(tenant_code, status, updated_at DESC);

CREATE TABLE crm_quote_line_items (
    id VARCHAR(36) NOT NULL,
    quote_id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    line_no INTEGER NOT NULL,
    product_name VARCHAR(240) NOT NULL,
    quantity DECIMAL(14,2) NOT NULL,
    unit_price DECIMAL(16,2) NOT NULL,
    discount_percent DECIMAL(7,4) NOT NULL DEFAULT 0,
    tax_percent DECIMAL(7,4) NOT NULL DEFAULT 0,
    line_total DECIMAL(16,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_crm_quote_line_items PRIMARY KEY (id),
    CONSTRAINT fk_crm_quote_line_items_quote FOREIGN KEY (quote_id) REFERENCES crm_quotes(id)
);

CREATE INDEX ix_crm_quote_line_items_quote ON crm_quote_line_items(quote_id, line_no);
