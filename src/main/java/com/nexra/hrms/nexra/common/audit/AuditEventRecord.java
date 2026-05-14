package com.nexra.hrms.nexra.common.audit;

/**
 * Immutable record representing an audit event to be persisted. All fields
 * are optional except for the mandatory core (tenantCode, module, action, outcome).
 * Modules build instances via {@link AuditEventRecord#of(String, String, String, String)}
 * and chain optional fields using the {@code with*} builder-style methods.
 *
 * @param tenantCode  tenant that the action belongs to.
 * @param module      logical module name (e.g. AUTH, PAYROLL).
 * @param action      action name in UPPER_SNAKE_CASE (e.g. LOGIN, CREATE_EMPLOYEE).
 * @param outcome     action result: SUCCESS or FAILURE.
 * @param actorEmail  email of the authenticated actor.
 * @param actorUserId UUID of the authenticated user.
 * @param targetType  entity type the action targeted.
 * @param targetId    ID of the targeted entity.
 * @param detail      free-form supplementary context (JSON recommended).
 * @param ipAddress   originating IP address.
 * @param requestId   correlation request ID.
 * @author niteshjaitwar
 */
public record AuditEventRecord(
    String tenantCode,
    String module,
    String action,
    String outcome,
    String actorEmail,
    String actorUserId,
    String targetType,
    String targetId,
    String detail,
    String ipAddress,
    String requestId
) {

    /**
     * Creates a minimal audit event with only the mandatory fields.
     *
     * @param tenantCode tenant code.
     * @param module     module name.
     * @param action     action name.
     * @param outcome    SUCCESS or FAILURE.
     * @return new AuditEventRecord with nulls for optional fields.
     */
    public static AuditEventRecord of(
        final String tenantCode,
        final String module,
        final String action,
        final String outcome
    ) {
        return new AuditEventRecord(tenantCode, module, action, outcome, null, null, null, null, null, null, null);
    }

    /** Returns a copy with actorEmail set. */
    public AuditEventRecord withActor(final String email, final String userId) {
        return new AuditEventRecord(tenantCode, module, action, outcome, email, userId, targetType, targetId, detail, ipAddress, requestId);
    }

    /** Returns a copy with target entity info set. */
    public AuditEventRecord withTarget(final String type, final String id) {
        return new AuditEventRecord(tenantCode, module, action, outcome, actorEmail, actorUserId, type, id, detail, ipAddress, requestId);
    }

    /** Returns a copy with detail blob set. */
    public AuditEventRecord withDetail(final String detailJson) {
        return new AuditEventRecord(tenantCode, module, action, outcome, actorEmail, actorUserId, targetType, targetId, detailJson, ipAddress, requestId);
    }

    /** Returns a copy with request tracking info set. */
    public AuditEventRecord withRequestInfo(final String ip, final String reqId) {
        return new AuditEventRecord(tenantCode, module, action, outcome, actorEmail, actorUserId, targetType, targetId, detail, ip, reqId);
    }
}
