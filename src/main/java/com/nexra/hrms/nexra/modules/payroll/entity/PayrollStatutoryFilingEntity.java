package com.nexra.hrms.nexra.modules.payroll.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * An immutable statutory filing artifact summarizing the statutory liabilities
 * for a tenant, country, and pay period (the analog of a PF ECR, US Form 941, or
 * UK RTI FPS submission record).
 */
@Entity
@Table(name = "payroll_statutory_filings")
public class PayrollStatutoryFilingEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "country_code", nullable = false, length = 4)
    private String countryCode;

    @Column(name = "period", nullable = false, length = 7)
    private String period;

    @Column(name = "filing_type", nullable = false, length = 60)
    private String filingType;

    @Column(name = "reference_number", nullable = false, length = 80)
    private String referenceNumber;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "employee_count", nullable = false)
    private int employeeCount;

    @Column(name = "total_gross", nullable = false, precision = 16, scale = 2)
    private BigDecimal totalGross;

    @Column(name = "employee_contribution", nullable = false, precision = 16, scale = 2)
    private BigDecimal employeeContribution;

    @Column(name = "employer_contribution", nullable = false, precision = 16, scale = 2)
    private BigDecimal employerContribution;

    @Column(name = "total_contribution", nullable = false, precision = 16, scale = 2)
    private BigDecimal totalContribution;

    @Column(name = "component_totals_json", nullable = false, columnDefinition = "TEXT")
    private String componentTotalsJson;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(final String countryCode) { this.countryCode = countryCode; }
    public String getPeriod() { return period; }
    public void setPeriod(final String period) { this.period = period; }
    public String getFilingType() { return filingType; }
    public void setFilingType(final String filingType) { this.filingType = filingType; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(final String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }
    public int getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(final int employeeCount) { this.employeeCount = employeeCount; }
    public BigDecimal getTotalGross() { return totalGross; }
    public void setTotalGross(final BigDecimal totalGross) { this.totalGross = totalGross; }
    public BigDecimal getEmployeeContribution() { return employeeContribution; }
    public void setEmployeeContribution(final BigDecimal employeeContribution) { this.employeeContribution = employeeContribution; }
    public BigDecimal getEmployerContribution() { return employerContribution; }
    public void setEmployerContribution(final BigDecimal employerContribution) { this.employerContribution = employerContribution; }
    public BigDecimal getTotalContribution() { return totalContribution; }
    public void setTotalContribution(final BigDecimal totalContribution) { this.totalContribution = totalContribution; }
    public String getComponentTotalsJson() { return componentTotalsJson; }
    public void setComponentTotalsJson(final String componentTotalsJson) { this.componentTotalsJson = componentTotalsJson; }
    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(final Instant generatedAt) { this.generatedAt = generatedAt; }
}
