package com.nexra.hrms.nexra.modules.payroll.entity;

import com.nexra.hrms.nexra.modules.payroll.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "payroll_employee_profiles")
public class PayrollEmployeeProfileEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

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

    @Column(name = "monthly_basic_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyBasicSalary;

    @Column(name = "bank_name", length = 120)
    private String bankName;

    @Column(name = "bank_account_masked", length = 80)
    private String bankAccountMasked;

    @Column(name = "pan_masked", length = 40)
    private String panMasked;

    @Column(name = "uan_masked", length = 40)
    private String uanMasked;

    @Column(name = "email", length = 180)
    private String email;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
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

    public BigDecimal getMonthlyBasicSalary() {
        return monthlyBasicSalary;
    }

    public void setMonthlyBasicSalary(final BigDecimal monthlyBasicSalary) {
        this.monthlyBasicSalary = monthlyBasicSalary;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(final String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountMasked() {
        return bankAccountMasked;
    }

    public void setBankAccountMasked(final String bankAccountMasked) {
        this.bankAccountMasked = bankAccountMasked;
    }

    public String getPanMasked() {
        return panMasked;
    }

    public void setPanMasked(final String panMasked) {
        this.panMasked = panMasked;
    }

    public String getUanMasked() {
        return uanMasked;
    }

    public void setUanMasked(final String uanMasked) {
        this.uanMasked = uanMasked;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
