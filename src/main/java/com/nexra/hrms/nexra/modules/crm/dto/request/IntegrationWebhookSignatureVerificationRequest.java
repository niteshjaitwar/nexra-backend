package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IntegrationWebhookSignatureVerificationRequest(
    @NotBlank
    @Size(max = 10000)
    String payloadJson,
    @NotBlank
    @Size(max = 200)
    String idempotencyKey,
    @NotBlank
    @Size(max = 40)
    String timestamp,
    @NotBlank
    @Size(min = 16, max = 256)
    String secret,
    @NotBlank
    @Size(max = 256)
    String signature
) {
}
