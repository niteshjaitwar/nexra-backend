package com.nexra.hrms.nexra.modules.payroll.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * A single salary band for a slab-driven statutory component (for example
 * professional tax). The applicable slab is the one whose gross range contains
 * the employee monthly gross.
 */
@Entity
@Table(name = "payroll_statutory_slabs")
public class PayrollStatutorySlabEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "country_code", nullable = false, length = 4)
    private String countryCode;

    @Column(name = "component_code", nullable = false, length = 60)
    private String componentCode;

    @Column(name = "min_gross", nullable = false, precision = 14, scale = 2)
    private BigDecimal minGross;

    @Column(name = "max_gross", precision = 14, scale = 2)
    private BigDecimal maxGross;

    @Column(name = "fixed_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal fixedAmount;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(final String countryCode) { this.countryCode = countryCode; }
    public String getComponentCode() { return componentCode; }
    public void setComponentCode(final String componentCode) { this.componentCode = componentCode; }
    public BigDecimal getMinGross() { return minGross; }
    public void setMinGross(final BigDecimal minGross) { this.minGross = minGross; }
    public BigDecimal getMaxGross() { return maxGross; }
    public void setMaxGross(final BigDecimal maxGross) { this.maxGross = maxGross; }
    public BigDecimal getFixedAmount() { return fixedAmount; }
    public void setFixedAmount(final BigDecimal fixedAmount) { this.fixedAmount = fixedAmount; }
    public boolean isActive() { return active; }
    public void setActive(final boolean active) { this.active = active; }
}
