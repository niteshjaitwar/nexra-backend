package com.nexra.hrms.nexra.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Append-only, immutable audit event record stored in the {@code audit_events} table.
 * This entity does NOT extend {@code BaseAuditableEntity} because audit events are
 * intentionally write-once and must not carry optimistic-lock or updatedBy columns.
 * The {@code created_at} timestamp is set via {@code @PrePersist} to ensure it is
 * always present even when Spring Data Auditing is not fully wired in tests.
 *
 * @author niteshjaitwar
 */
@Entity
@Table(name = "audit_events")
@Getter
@Setter
@NoArgsConstructor
public class AuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Stable UUID for idempotent audit writes. */
    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    /** Tenant scope. */
    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    /** Logical module name (e.g. AUTH, EMPLOYEE_CORE, PAYROLL). */
    @Column(name = "module", nullable = false, length = 60)
    private String module;

    /** Action performed (e.g. LOGIN, LOGOUT, CREATE_EMPLOYEE, APPROVE_LEAVE). */
    @Column(name = "action", nullable = false, length = 120)
    private String action;

    /** Outcome of the action: SUCCESS or FAILURE. */
    @Column(name = "outcome", nullable = false, length = 20)
    private String outcome;

    /** Email of the authenticated actor, null for anonymous actions. */
    @Column(name = "actor_email", length = 255)
    private String actorEmail;

    /** UUID of the authenticated user, null for anonymous actions. */
    @Column(name = "actor_user_id", length = 36)
    private String actorUserId;

    /** Domain entity type the action targeted (e.g. Employee, LeaveRequest). */
    @Column(name = "target_type", length = 120)
    private String targetType;

    /** ID of the targeted entity. */
    @Column(name = "target_id", length = 36)
    private String targetId;

    /** Free-form JSON detail blob for supplementary context. */
    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    /** IP address of the originating request. */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** Correlation / request ID from the X-Request-Id header. */
    @Column(name = "request_id", length = 36)
    private String requestId;

    /** UTC instant the row was first persisted. Never updated. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Sets createdAt before the entity is first persisted if not already set. */
    @PrePersist
    protected void onPrePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
