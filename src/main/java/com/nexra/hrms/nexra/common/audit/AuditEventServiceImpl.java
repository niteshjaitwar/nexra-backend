package com.nexra.hrms.nexra.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link AuditEventService}. Persists audit events
 * in a new, independent transaction so that a calling business transaction
 * rollback does NOT discard the audit record. Failures during persistence are
 * caught and logged — they MUST NOT propagate to the caller.
 *
 * <p>Methods annotated with {@code @Async} require that
 * {@code @EnableAsync} is active on the application context.
 *
 * @author niteshjaitwar
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditEventServiceImpl implements AuditEventService {

    private static final int MAX_LIMIT = 200;

    private final AuditEventRepository repository;

    /**
     * {@inheritDoc}
     *
     * <p>Runs in its own new transaction ({@code REQUIRES_NEW}) so that a
     * rollback in the caller's transaction does not erase the audit record.
     * Any persistence failure is silently swallowed after logging.
     */
    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(final AuditEventRecord event) {
        try {
            AuditEventEntity entity = toEntity(event);
            repository.save(entity);
            log.debug("AuditEventServiceImpl - recorded audit event module={} action={} outcome={} tenant={}",
                event.module(), event.action(), event.outcome(), event.tenantCode());
        } catch (Exception ex) {
            // Audit failures MUST NOT interrupt the calling business flow.
            log.error("AuditEventServiceImpl - failed to persist audit event module={} action={} - {}",
                event.module(), event.action(), ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditEventView> getRecentByTenant(final String tenantCode, final int limit) {
        int safeLimit = Math.min(limit, MAX_LIMIT);
        return repository.findRecentByTenant(tenantCode.toUpperCase(), safeLimit).stream()
            .map(this::toView)
            .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuditEventView> getByTenantAndModule(
        final String tenantCode,
        final String module,
        final int limit
    ) {
        int safeLimit = Math.min(limit, MAX_LIMIT);
        return repository.findByTenantAndModule(tenantCode.toUpperCase(), module.toUpperCase(), safeLimit).stream()
            .map(this::toView)
            .toList();
    }

    // ---- private helpers ----

    private AuditEventEntity toEntity(final AuditEventRecord event) {
        AuditEventEntity entity = new AuditEventEntity();
        entity.setEventId(UUID.randomUUID().toString());
        entity.setTenantCode(event.tenantCode() != null ? event.tenantCode().toUpperCase() : "PLATFORM");
        entity.setModule(event.module() != null ? event.module().toUpperCase() : "UNKNOWN");
        entity.setAction(event.action() != null ? event.action().toUpperCase() : "UNKNOWN");
        entity.setOutcome(event.outcome() != null ? event.outcome().toUpperCase() : "SUCCESS");
        entity.setActorEmail(event.actorEmail());
        entity.setActorUserId(event.actorUserId());
        entity.setTargetType(event.targetType());
        entity.setTargetId(event.targetId());
        entity.setDetail(event.detail());
        entity.setIpAddress(event.ipAddress());
        entity.setRequestId(event.requestId());
        return entity;
    }

    private AuditEventView toView(final AuditEventEntity entity) {
        return new AuditEventView(
            entity.getEventId(),
            entity.getTenantCode(),
            entity.getModule(),
            entity.getAction(),
            entity.getOutcome(),
            entity.getActorEmail(),
            entity.getTargetType(),
            entity.getTargetId(),
            entity.getRequestId(),
            entity.getCreatedAt()
        );
    }
}
