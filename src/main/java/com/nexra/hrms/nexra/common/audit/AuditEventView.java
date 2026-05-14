package com.nexra.hrms.nexra.common.audit;

import java.time.Instant;

/**
 * Read-only view of an audit event for API responses.
 *
 * @param eventId     stable UUID for the event.
 * @param tenantCode  tenant that owns the event.
 * @param module      module that generated the event.
 * @param action      action performed.
 * @param outcome     SUCCESS or FAILURE.
 * @param actorEmail  email of the actor.
 * @param targetType  entity type targeted.
 * @param targetId    ID of the targeted entity.
 * @param requestId   correlation request ID.
 * @param createdAt   timestamp when the event was recorded.
 * @author niteshjaitwar
 */
public record AuditEventView(
    String eventId,
    String tenantCode,
    String module,
    String action,
    String outcome,
    String actorEmail,
    String targetType,
    String targetId,
    String requestId,
    Instant createdAt
) {
}
