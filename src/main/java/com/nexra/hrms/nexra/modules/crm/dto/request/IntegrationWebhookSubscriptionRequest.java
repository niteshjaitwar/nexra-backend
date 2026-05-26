package com.nexra.hrms.nexra.modules.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IntegrationWebhookSubscriptionRequest(
    @NotBlank
    @Size(max = 120)
    String eventType,
    @NotBlank
    @Size(max = 500)
    String targetUrl,
    @NotBlank
    @Size(min = 16, max = 256)
    String secret,
    boolean active
) {
}
