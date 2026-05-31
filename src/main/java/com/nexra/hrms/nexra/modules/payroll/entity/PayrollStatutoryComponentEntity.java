package com.nexra.hrms.nexra.modules.payroll.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "payroll_statutory_components")
public class PayrollStatutoryComponentEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "country_code", nullable = false, length = 4)
    private String countryCode;

    @Column(name = "component_code", nullable = false, length = 60)
    private String componentCode;

    @Column(name = "component_name", nullable = false, length = 160)
    private String componentName;

    @Column(name = "component_type", nullable = false, length = 20)
    private String componentType;

    @Column(name = "rate_percent", precision = 8, scale = 4)
    private BigDecimal ratePercent;

    @Column(name = "fixed_amount", precision = 14, scale = 2)
    private BigDecimal fixedAmount;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "employer_borne", nullable = false)
    private boolean employerBorne = false;

    @Column(name = "wage_ceiling", precision = 14, scale = 2)
    private BigDecimal wageCeiling;

    @Column(name = "wage_floor", precision = 14, scale = 2)
    private BigDecimal wageFloor;

    @Column(name = "min_gross", precision = 14, scale = 2)
    private BigDecimal minGross;

    @Column(name = "max_gross", precision = 14, scale = 2)
    private BigDecimal maxGross;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(final String countryCode) { this.countryCode = countryCode; }
    public String getComponentCode() { return componentCode; }
    public void setComponentCode(final String componentCode) { this.componentCode = componentCode; }
    public String getComponentName() { return componentName; }
    public void setComponentName(final String componentName) { this.componentName = componentName; }
    public String getComponentType() { return componentType; }
    public void setComponentType(final String componentType) { this.componentType = componentType; }
    public BigDecimal getRatePercent() { return ratePercent; }
    public void setRatePercent(final BigDecimal ratePercent) { this.ratePercent = ratePercent; }
    public BigDecimal getFixedAmount() { return fixedAmount; }
    public void setFixedAmount(final BigDecimal fixedAmount) { this.fixedAmount = fixedAmount; }
    public boolean isActive() { return active; }
    public void setActive(final boolean active) { this.active = active; }
    public boolean isEmployerBorne() { return employerBorne; }
    public void setEmployerBorne(final boolean employerBorne) { this.employerBorne = employerBorne; }
    public BigDecimal getWageCeiling() { return wageCeiling; }
    public void setWageCeiling(final BigDecimal wageCeiling) { this.wageCeiling = wageCeiling; }
    public BigDecimal getWageFloor() { return wageFloor; }
    public void setWageFloor(final BigDecimal wageFloor) { this.wageFloor = wageFloor; }
    public BigDecimal getMinGross() { return minGross; }
    public void setMinGross(final BigDecimal minGross) { this.minGross = minGross; }
    public BigDecimal getMaxGross() { return maxGross; }
    public void setMaxGross(final BigDecimal maxGross) { this.maxGross = maxGross; }
}
