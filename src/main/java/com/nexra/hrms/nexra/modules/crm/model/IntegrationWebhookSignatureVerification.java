package com.nexra.hrms.nexra.modules.crm.model;

public record IntegrationWebhookSignatureVerification(
    boolean valid,
    String algorithm
) {
}
