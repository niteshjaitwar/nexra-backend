package com.nexra.hrms.nexra.modules.crm.model;

import java.time.Instant;

public record IntegrationWebhookSubscription(
    String id,
    String tenantCode,
    String productKey,
    String eventType,
    String targetUrl,
    boolean active,
    int failureCount,
    Instant lastSuccessAt,
    Instant lastFailureAt,
    Instant createdAt,
    Instant updatedAt
) {
}
