package com.nexra.hrms.nexra.modules.crm.model;

import java.time.Instant;

public record IntegrationWebhookReplayAuditView(
    Instant createdAt,
    String actorEmail,
    String actorUserId,
    String targetDeliveryId,
    String outcome
) {
}
