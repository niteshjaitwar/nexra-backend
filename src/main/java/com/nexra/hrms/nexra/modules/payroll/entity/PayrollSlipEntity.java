package com.nexra.hrms.nexra.modules.payroll.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Persists immutable payroll slip snapshots generated for a tenant employee
 * and pay period. The entity extends the shared auditable base to enforce
 * optimistic locking and common audit metadata standards across modules.
 *
 * @author niteshjaitwar
 */
@Entity
@Table(name = "payroll_slips")
public class PayrollSlipEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "slip_id", nullable = false, length = 36)
    private String slipId;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "employee_id", nullable = false, length = 64)
    private String employeeId;

    @Column(name = "employee_code", nullable = false, length = 64)
    private String employeeCode;

    @Column(name = "employee_name", nullable = false, length = 180)
    private String employeeName;

    @Column(name = "department", nullable = false, length = 120)
    private String department;

    @Column(name = "designation", nullable = false, length = 120)
    private String designation;

    @Column(name = "pay_period", nullable = false, length = 20)
    private String payPeriod;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "organization_profile_json", nullable = false, columnDefinition = "LONGTEXT")
    private String organizationProfileJson;

    @Column(name = "employee_profile_json", columnDefinition = "LONGTEXT")
    private String employeeProfileJson;

    @Column(name = "allowances_json", nullable = false, columnDefinition = "LONGTEXT")
    private String allowancesJson;

    @Column(name = "deductions_json", nullable = false, columnDefinition = "LONGTEXT")
    private String deductionsJson;

    @Column(name = "auth_dependency_status_json", nullable = false, columnDefinition = "LONGTEXT")
    private String authDependencyStatusJson;

    @Column(name = "basic_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal basicSalary;

    @Column(name = "tax_percent", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxPercent;

    @Column(name = "provident_fund_percent", nullable = false, precision = 10, scale = 2)
    private BigDecimal providentFundPercent;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "provident_fund_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal providentFundAmount;

    @Column(name = "gross_earnings", nullable = false, precision = 19, scale = 2)
    private BigDecimal grossEarnings;

    @Column(name = "total_deductions", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalDeductions;

    @Column(name = "net_pay", nullable = false, precision = 19, scale = 2)
    private BigDecimal netPay;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(name = "generated_by_email", nullable = false, length = 180)
    private String generatedByEmail;

    @Column(name = "generated_by_user_id", nullable = false, length = 36)
    private String generatedByUserId;

    public String getSlipId() {
        return slipId;
    }

    public void setSlipId(final String slipId) {
        this.slipId = slipId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(final String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(final String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(final String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(final String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(final String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(final String designation) {
        this.designation = designation;
    }

    public String getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(final String payPeriod) {
        this.payPeriod = payPeriod;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getOrganizationProfileJson() {
        return organizationProfileJson;
    }

    public void setOrganizationProfileJson(final String organizationProfileJson) {
        this.organizationProfileJson = organizationProfileJson;
    }

    public String getEmployeeProfileJson() {
        return employeeProfileJson;
    }

    public void setEmployeeProfileJson(final String employeeProfileJson) {
        this.employeeProfileJson = employeeProfileJson;
    }

    public String getAllowancesJson() {
        return allowancesJson;
    }

    public void setAllowancesJson(final String allowancesJson) {
        this.allowancesJson = allowancesJson;
    }

    public String getDeductionsJson() {
        return deductionsJson;
    }

    public void setDeductionsJson(final String deductionsJson) {
        this.deductionsJson = deductionsJson;
    }

    public String getAuthDependencyStatusJson() {
        return authDependencyStatusJson;
    }

    public void setAuthDependencyStatusJson(final String authDependencyStatusJson) {
        this.authDependencyStatusJson = authDependencyStatusJson;
    }

    public BigDecimal getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(final BigDecimal basicSalary) {
        this.basicSalary = basicSalary;
    }

    public BigDecimal getTaxPercent() {
        return taxPercent;
    }

    public void setTaxPercent(final BigDecimal taxPercent) {
        this.taxPercent = taxPercent;
    }

    public BigDecimal getProvidentFundPercent() {
        return providentFundPercent;
    }

    public void setProvidentFundPercent(final BigDecimal providentFundPercent) {
        this.providentFundPercent = providentFundPercent;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(final BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getProvidentFundAmount() {
        return providentFundAmount;
    }

    public void setProvidentFundAmount(final BigDecimal providentFundAmount) {
        this.providentFundAmount = providentFundAmount;
    }

    public BigDecimal getGrossEarnings() {
        return grossEarnings;
    }

    public void setGrossEarnings(final BigDecimal grossEarnings) {
        this.grossEarnings = grossEarnings;
    }

    public BigDecimal getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(final BigDecimal totalDeductions) {
        this.totalDeductions = totalDeductions;
    }

    public BigDecimal getNetPay() {
        return netPay;
    }

    public void setNetPay(final BigDecimal netPay) {
        this.netPay = netPay;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(final Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getGeneratedByEmail() {
        return generatedByEmail;
    }

    public void setGeneratedByEmail(final String generatedByEmail) {
        this.generatedByEmail = generatedByEmail;
    }

    public String getGeneratedByUserId() {
        return generatedByUserId;
    }

    public void setGeneratedByUserId(final String generatedByUserId) {
        this.generatedByUserId = generatedByUserId;
    }
}
