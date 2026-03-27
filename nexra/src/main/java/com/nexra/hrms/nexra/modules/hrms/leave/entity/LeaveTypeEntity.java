package com.nexra.hrms.nexra.modules.hrms.leave.entity;

import com.nexra.hrms.nexra.modules.hrms.leave.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "lv_leave_types")
public class LeaveTypeEntity extends AbstractAuditableEntity {
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;
    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;
    @Column(name = "code", nullable = false, length = 40)
    private String code;
    @Column(name = "name", nullable = false, length = 120)
    private String name;
    @Column(name = "paid", nullable = false)
    private boolean paid;
    @Column(name = "default_annual_quota", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultAnnualQuota;
    @Column(name = "active", nullable = false)
    private boolean active;
    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getCode() { return code; }
    public void setCode(final String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }
    public boolean isPaid() { return paid; }
    public void setPaid(final boolean paid) { this.paid = paid; }
    public BigDecimal getDefaultAnnualQuota() { return defaultAnnualQuota; }
    public void setDefaultAnnualQuota(final BigDecimal defaultAnnualQuota) { this.defaultAnnualQuota = defaultAnnualQuota; }
    public boolean isActive() { return active; }
    public void setActive(final boolean active) { this.active = active; }
}

