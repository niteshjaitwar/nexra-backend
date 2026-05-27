package com.nexra.hrms.nexra.modules.crm.model;

import java.time.Instant;

public record IntegrationWebhookDelivery(
    String id,
    String subscriptionId,
    String eventType,
    String status,
    int attemptCount,
    int maxAttempts,
    Instant nextAttemptAt,
    Instant deliveredAt,
    Instant lastFailureAt,
    Integer lastStatusCode,
    String lastError,
    Instant createdAt,
    Instant updatedAt
) {
}
