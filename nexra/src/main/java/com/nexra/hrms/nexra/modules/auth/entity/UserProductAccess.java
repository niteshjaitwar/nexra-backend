package com.nexra.hrms.nexra.modules.auth.entity;

import com.nexra.hrms.nexra.modules.auth.entity.base.AbstractAuditableEntity;
import com.nexra.hrms.nexra.modules.auth.enums.ProductRole;
import com.nexra.hrms.nexra.modules.auth.enums.ProductType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * Tracks product-level access grants for tenant users across HRMS and CRM products.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Entity
@Table(
    name = "user_product_access",
    uniqueConstraints = @UniqueConstraint(name = "uk_user_product", columnNames = {"user_id", "product"})
)
@Getter
@Setter
public class UserProductAccess extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductType product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProductRole productRole;

    @Column(nullable = false)
    private Instant grantedAt;

    @Column(length = 36)
    private String grantedBy;
}
