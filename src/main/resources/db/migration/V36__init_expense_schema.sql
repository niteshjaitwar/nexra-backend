CREATE TABLE ex_categories (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(120) NOT NULL,
    max_amount_per_claim DECIMAL(14,2) NULL,
    requires_receipt BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_ex_categories PRIMARY KEY (id),
    CONSTRAINT uk_ex_categories_tenant_code UNIQUE (tenant_code, code)
);

CREATE TABLE ex_claims (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    employee_id VARCHAR(36) NOT NULL,
    claim_date DATE NOT NULL,
    title VARCHAR(200) NOT NULL,
    currency VARCHAR(12) NOT NULL,
    total_amount DECIMAL(14,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    approver_user_id VARCHAR(36) NULL,
    approver_email VARCHAR(160) NULL,
    approval_comment VARCHAR(500) NULL,
    reimbursed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_ex_claims PRIMARY KEY (id)
);

CREATE TABLE ex_claim_items (
    id VARCHAR(36) NOT NULL,
    claim_id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    expense_date DATE NOT NULL,
    category_code VARCHAR(40) NOT NULL,
    description_text VARCHAR(300) NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    receipt_reference VARCHAR(200) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_ex_claim_items PRIMARY KEY (id),
    CONSTRAINT fk_ex_claim_items_claim FOREIGN KEY (claim_id) REFERENCES ex_claims(id)
);

CREATE INDEX idx_ex_claims_tenant_status ON ex_claims(tenant_code, status);
CREATE INDEX idx_ex_claims_tenant_employee ON ex_claims(tenant_code, employee_id);
CREATE INDEX idx_ex_claim_items_claim ON ex_claim_items(claim_id);
