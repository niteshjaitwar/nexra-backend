package com.nexra.hrms.nexra.modules.crm.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "integration_webhook_deliveries")
public class IntegrationWebhookDeliveryEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private IntegrationWebhookSubscriptionEntity subscription;

    @Column(name = "product_key", nullable = false, length = 40)
    private String productKey;

    @Column(name = "event_type", nullable = false, length = 120)
    private String eventType;

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "payload_hash", nullable = false, length = 128)
    private String payloadHash;

    @Column(name = "idempotency_key", nullable = false, length = 80)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private IntegrationWebhookDeliveryStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "last_failure_at")
    private Instant lastFailureAt;

    @Column(name = "last_status_code")
    private Integer lastStatusCode;

    @Column(name = "last_error", length = 500)
    private String lastError;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public IntegrationWebhookSubscriptionEntity getSubscription() { return subscription; }
    public void setSubscription(final IntegrationWebhookSubscriptionEntity subscription) { this.subscription = subscription; }
    public String getProductKey() { return productKey; }
    public void setProductKey(final String productKey) { this.productKey = productKey; }
    public String getEventType() { return eventType; }
    public void setEventType(final String eventType) { this.eventType = eventType; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(final String payloadJson) { this.payloadJson = payloadJson; }
    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(final String payloadHash) { this.payloadHash = payloadHash; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(final String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public IntegrationWebhookDeliveryStatus getStatus() { return status; }
    public void setStatus(final IntegrationWebhookDeliveryStatus status) { this.status = status; }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(final int attemptCount) { this.attemptCount = attemptCount; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(final int maxAttempts) { this.maxAttempts = maxAttempts; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public void setNextAttemptAt(final Instant nextAttemptAt) { this.nextAttemptAt = nextAttemptAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(final Instant deliveredAt) { this.deliveredAt = deliveredAt; }
    public Instant getLastFailureAt() { return lastFailureAt; }
    public void setLastFailureAt(final Instant lastFailureAt) { this.lastFailureAt = lastFailureAt; }
    public Integer getLastStatusCode() { return lastStatusCode; }
    public void setLastStatusCode(final Integer lastStatusCode) { this.lastStatusCode = lastStatusCode; }
    public String getLastError() { return lastError; }
    public void setLastError(final String lastError) { this.lastError = lastError; }
}
