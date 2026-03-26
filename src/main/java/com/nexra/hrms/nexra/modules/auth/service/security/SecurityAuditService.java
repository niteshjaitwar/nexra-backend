package com.nexra.hrms.nexra.modules.auth.service.security;

/**
 * Records high-signal security events for production audit trails.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public interface SecurityAuditService {

    /**
     * Records a security-relevant event with normalized contextual metadata.
     *
     * @param eventType stable event identifier
     * @param tenantCode tenant code when available
     * @param email email when available
     * @param outcome result classification
     * @param details short operational details
     */
    void record(String eventType, String tenantCode, String email, String outcome, String details);
}
