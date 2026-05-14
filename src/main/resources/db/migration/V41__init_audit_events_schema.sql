-- V41: Platform-wide audit events table (append-only, immutable record of high-value mutations)
-- This table is written to by the shared AuditEventService and MUST NEVER be updated or deleted.
-- Retention: retained for at least 7 years for compliance purposes.

CREATE TABLE IF NOT EXISTS audit_events (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    event_id        VARCHAR(36)     NOT NULL,
    tenant_code     VARCHAR(60)     NOT NULL,
    module          VARCHAR(60)     NOT NULL,
    action          VARCHAR(120)    NOT NULL,
    outcome         VARCHAR(20)     NOT NULL    COMMENT 'SUCCESS or FAILURE',
    actor_email     VARCHAR(255),
    actor_user_id   VARCHAR(36),
    target_type     VARCHAR(120),
    target_id       VARCHAR(36),
    detail          TEXT,
    ip_address      VARCHAR(45),
    request_id      VARCHAR(36),
    created_at      DATETIME(3)     NOT NULL    DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uq_audit_events_event_id (event_id),
    INDEX idx_audit_events_tenant_module (tenant_code, module),
    INDEX idx_audit_events_tenant_actor (tenant_code, actor_email),
    INDEX idx_audit_events_tenant_created (tenant_code, created_at),
    INDEX idx_audit_events_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Append-only immutable audit trail for all high-value platform actions';
