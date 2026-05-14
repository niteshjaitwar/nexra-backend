package com.nexra.hrms.nexra.common.audit;

import java.util.List;

/**
 * Platform-wide audit event service contract. All modules MUST use this service
 * to record high-value mutations instead of writing directly to any audit table.
 * Implementations are expected to be non-blocking and non-interrupting — a failure
 * to persist an audit event MUST NOT cause the calling business transaction to fail.
 *
 * @author niteshjaitwar
 */
public interface AuditEventService {

    /**
     * Records an audit event asynchronously. Any persistence failure is silently
     * logged and swallowed — callers MUST NOT rely on this method throwing exceptions.
     *
     * @param event the audit event to record.
     */
    void record(AuditEventRecord event);

    /**
     * Returns the most recent audit events for a tenant across all modules.
     *
     * @param tenantCode tenant to scope the query.
     * @param limit      max number of records to return (capped at 200).
     * @return list of audit event views ordered by creation date descending.
     */
    List<AuditEventView> getRecentByTenant(String tenantCode, int limit);

    /**
     * Returns audit events for a specific module within a tenant.
     *
     * @param tenantCode tenant to scope the query.
     * @param module     module name filter.
     * @param limit      max number of records to return (capped at 200).
     * @return list of audit event views ordered by creation date descending.
     */
    List<AuditEventView> getByTenantAndModule(String tenantCode, String module, int limit);
}
