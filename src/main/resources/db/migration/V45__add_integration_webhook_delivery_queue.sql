CREATE TABLE integration_webhook_deliveries (
    id VARCHAR(36) NOT NULL,
    tenant_code VARCHAR(60) NOT NULL,
    subscription_id VARCHAR(36) NOT NULL,
    product_key VARCHAR(40) NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    payload_json TEXT NOT NULL,
    payload_hash VARCHAR(128) NOT NULL,
    idempotency_key VARCHAR(80) NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    max_attempts INTEGER NOT NULL DEFAULT 5,
    next_attempt_at TIMESTAMP NOT NULL,
    delivered_at TIMESTAMP NULL,
    last_failure_at TIMESTAMP NULL,
    last_status_code INTEGER NULL,
    last_error VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(120) NOT NULL,
    updated_by VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_integration_webhook_deliveries PRIMARY KEY (id),
    CONSTRAINT fk_integration_webhook_deliveries_subscription
        FOREIGN KEY (subscription_id) REFERENCES integration_webhook_subscriptions(id),
    CONSTRAINT uk_integration_webhook_deliveries_idempotency UNIQUE (idempotency_key)
);

CREATE INDEX ix_integration_webhook_deliveries_due
    ON integration_webhook_deliveries(status, next_attempt_at, tenant_code);
