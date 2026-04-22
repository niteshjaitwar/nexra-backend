package com.nexra.hrms.nexra.modules.hrms.expense.entity;

import com.nexra.hrms.nexra.modules.hrms.expense.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "ex_categories")
public class ExpenseCategoryEntity extends AbstractAuditableEntity {
    @Id @Column(name = "id", nullable = false, length = 36) private String id;
    @Column(name = "tenant_code", nullable = false, length = 60) private String tenantCode;
    @Column(name = "code", nullable = false, length = 40) private String code;
    @Column(name = "name", nullable = false, length = 120) private String name;
    @Column(name = "max_amount_per_claim", precision = 14, scale = 2) private BigDecimal maxAmountPerClaim;
    @Column(name = "requires_receipt", nullable = false) private boolean requiresReceipt;
    @Column(name = "active", nullable = false) private boolean active;
    public String getId() { return id; } public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; } public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getCode() { return code; } public void setCode(final String code) { this.code = code; }
    public String getName() { return name; } public void setName(final String name) { this.name = name; }
    public BigDecimal getMaxAmountPerClaim() { return maxAmountPerClaim; } public void setMaxAmountPerClaim(final BigDecimal maxAmountPerClaim) { this.maxAmountPerClaim = maxAmountPerClaim; }
    public boolean isRequiresReceipt() { return requiresReceipt; } public void setRequiresReceipt(final boolean requiresReceipt) { this.requiresReceipt = requiresReceipt; }
    public boolean isActive() { return active; } public void setActive(final boolean active) { this.active = active; }
}

