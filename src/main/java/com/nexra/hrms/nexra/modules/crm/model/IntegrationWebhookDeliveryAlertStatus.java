package com.nexra.hrms.nexra.modules.crm.model;

public record IntegrationWebhookDeliveryAlertStatus(
    long retryingCount,
    int retryingThreshold,
    boolean retryingThresholdBreached,
    long deadLetterCount,
    int deadLetterThreshold,
    boolean deadLetterThresholdBreached
) {
}
