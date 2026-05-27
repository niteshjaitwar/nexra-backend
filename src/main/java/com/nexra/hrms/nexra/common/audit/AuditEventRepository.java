package com.nexra.hrms.nexra.common.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * Repository for audit event persistence. Exposes only query methods — no
 * save is exposed publicly since all writes MUST go through {@link AuditEventService}.
 *
 * @author niteshjaitwar
 */
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {

    /**
     * Returns the most recent N audit events for a tenant across all modules.
     *
     * @param tenantCode tenant to scope the query.
     * @param limit      max number of rows to return.
     * @return list ordered by creation date descending.
     */
    @Query(value = "SELECT * FROM audit_events WHERE tenant_code = :tenantCode ORDER BY created_at DESC LIMIT :limit",
           nativeQuery = true)
    List<AuditEventEntity> findRecentByTenant(@Param("tenantCode") String tenantCode, @Param("limit") int limit);

    /**
     * Returns audit events for a specific module within a tenant.
     *
     * @param tenantCode tenant to scope the query.
     * @param module     module to filter on.
     * @param limit      max number of rows to return.
     * @return list ordered by creation date descending.
     */
    @Query(value = "SELECT * FROM audit_events WHERE tenant_code = :tenantCode AND module = :module ORDER BY created_at DESC LIMIT :limit",
           nativeQuery = true)
    List<AuditEventEntity> findByTenantAndModule(
        @Param("tenantCode") String tenantCode,
        @Param("module") String module,
        @Param("limit") int limit
    );

    @Query(value = """
        SELECT *
        FROM audit_events
        WHERE tenant_code = :tenantCode
          AND module = :module
          AND action = :action
        ORDER BY created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<AuditEventEntity> findByTenantModuleAndAction(
        @Param("tenantCode") String tenantCode,
        @Param("module") String module,
        @Param("action") String action,
        @Param("limit") int limit
    );

    /**
     * Returns audit events for a specific actor within a tenant.
     *
     * @param tenantCode  tenant to scope the query.
     * @param actorEmail  email of the actor.
     * @param limit       max number of rows to return.
     * @return list ordered by creation date descending.
     */
    @Query(value = "SELECT * FROM audit_events WHERE tenant_code = :tenantCode AND actor_email = :actorEmail ORDER BY created_at DESC LIMIT :limit",
           nativeQuery = true)
    List<AuditEventEntity> findByTenantAndActor(
        @Param("tenantCode") String tenantCode,
        @Param("actorEmail") String actorEmail,
        @Param("limit") int limit
    );
}
