package com.nexra.hrms.nexra.modules.auth.entity.base;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Base auditable entity with identifier and automatic created or updated metadata.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@MappedSuperclass
@Getter
public abstract class AbstractAuditableEntity extends BaseAuditableEntity {

    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;
}
