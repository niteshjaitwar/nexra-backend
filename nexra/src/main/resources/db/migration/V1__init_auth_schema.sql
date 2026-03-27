CREATE TABLE tenants (
    id CHAR(36) NOT NULL,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(120) NOT NULL,
    enterprise BOOLEAN NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_tenants PRIMARY KEY (id),
    CONSTRAINT uk_tenants_code UNIQUE (code)
);

CREATE TABLE user_accounts (
    id CHAR(36) NOT NULL,
    tenant_id CHAR(36) NOT NULL,
    email VARCHAR(160) NOT NULL,
    password_hash VARCHAR(160) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email_verified BOOLEAN NOT NULL,
    mfa_enabled BOOLEAN NOT NULL,
    account_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_user_accounts PRIMARY KEY (id),
    CONSTRAINT fk_user_accounts_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT uk_user_tenant_email UNIQUE (tenant_id, email)
);

CREATE TABLE user_roles (
    user_id CHAR(36) NOT NULL,
    role VARCHAR(40) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES user_accounts(id)
);

CREATE TABLE verification_tokens (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    type VARCHAR(40) NOT NULL,
    purpose VARCHAR(60) NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP NULL,
    delivery_target VARCHAR(160) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_verification_tokens PRIMARY KEY (id),
    CONSTRAINT fk_verification_tokens_user FOREIGN KEY (user_id) REFERENCES user_accounts(id)
);

CREATE TABLE refresh_tokens (
    id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES user_accounts(id)
);

CREATE INDEX idx_tenants_code ON tenants(code);
CREATE INDEX idx_users_tenant_email ON user_accounts(tenant_id, email);
CREATE INDEX idx_verification_user_purpose_type ON verification_tokens(user_id, purpose, type);
CREATE INDEX idx_verification_token_hash ON verification_tokens(token_hash);
CREATE INDEX idx_refresh_token_hash ON refresh_tokens(token_hash);
