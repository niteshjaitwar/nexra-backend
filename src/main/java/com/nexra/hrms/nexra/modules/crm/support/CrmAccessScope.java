package com.nexra.hrms.nexra.modules.crm.support;

/**
 * Encapsulates CRM record-access scope for the current request.
 *
 * @param actorUserId actor user identifier.
 * @param privileged whether actor can access all tenant records.
 */
public record CrmAccessScope(
    String actorUserId,
    boolean privileged
) {
}

