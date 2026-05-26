package com.nexra.hrms.nexra.modules.crm.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "integration_webhook_subscriptions")
public class IntegrationWebhookSubscriptionEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "product_key", nullable = false, length = 40)
    private String productKey;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(name = "target_url", nullable = false, length = 500)
    private String targetUrl;

    @Column(name = "secret_hash", nullable = false, length = 128)
    private String secretHash;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "failure_count", nullable = false)
    private int failureCount;

    @Column(name = "last_success_at")
    private Instant lastSuccessAt;

    @Column(name = "last_failure_at")
    private Instant lastFailureAt;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getProductKey() { return productKey; }
    public void setProductKey(final String productKey) { this.productKey = productKey; }
    public String getEventType() { return eventType; }
    public void setEventType(final String eventType) { this.eventType = eventType; }
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(final String targetUrl) { this.targetUrl = targetUrl; }
    public String getSecretHash() { return secretHash; }
    public void setSecretHash(final String secretHash) { this.secretHash = secretHash; }
    public boolean isActive() { return active; }
    public void setActive(final boolean active) { this.active = active; }
    public int getFailureCount() { return failureCount; }
    public void setFailureCount(final int failureCount) { this.failureCount = failureCount; }
    public Instant getLastSuccessAt() { return lastSuccessAt; }
    public void setLastSuccessAt(final Instant lastSuccessAt) { this.lastSuccessAt = lastSuccessAt; }
    public Instant getLastFailureAt() { return lastFailureAt; }
    public void setLastFailureAt(final Instant lastFailureAt) { this.lastFailureAt = lastFailureAt; }
}
