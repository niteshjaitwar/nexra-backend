package com.nexra.hrms.nexra.modules.crm.model;

public record IntegrationWebhookDeliveryMetrics(
    long pendingCount,
    long retryingCount,
    long succeededCount,
    long deadLetterCount,
    long totalCount
) {
}
