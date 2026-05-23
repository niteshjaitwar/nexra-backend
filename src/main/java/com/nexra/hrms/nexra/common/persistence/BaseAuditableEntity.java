package com.nexra.hrms.nexra.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Canonical auditable base for every JPA entity in the Nexra platform. Adds
 * optimistic locking via {@code @Version}, UTC creation and update timestamps,
 * and user audit fields wired through Spring Data auditing.
 *
 * @author niteshjaitwar
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditableEntity {

    /**
     * Monotonically increasing version used by Hibernate for optimistic
     * locking. Concurrent updates to stale entities throw
     * OptimisticLockingFailureException which the common handler maps to
     * HTTP 409 CONFLICT.
     */
    @Version
    @Column(name = "version", nullable = false)
    private long version;

    /**
     * UTC instant the row was first persisted. Never updated.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * UTC instant the row was last updated. Refreshed on every flush.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Principal name of the creator, resolved via AuditorAwareResolver.
     */
    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 120)
    private String createdBy;

    /**
     * Principal name of the last modifier, resolved via AuditorAwareResolver.
     */
    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 120)
    private String updatedBy;
}
