package com.nexra.hrms.nexra.modules.hrms.employee.entity;

import com.nexra.hrms.nexra.modules.hrms.employee.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ec_employees")
public class EmployeeEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "employee_code", nullable = false, length = 60)
    private String employeeCode;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "work_email", nullable = false, length = 160)
    private String workEmail;

    @Column(name = "department_id", length = 36)
    private String departmentId;

    @Column(name = "designation", nullable = false, length = 120)
    private String designation;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Column(name = "monthly_basic_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal monthlyBasicSalary;

    @Column(name = "bank_name", length = 120)
    private String bankName;

    @Column(name = "bank_account_masked", length = 50)
    private String bankAccountMasked;

    @Column(name = "pan_masked", length = 30)
    private String panMasked;

    @Column(name = "uan_masked", length = 30)
    private String uanMasked;

    @Column(name = "user_account_id", length = 36)
    private String userAccountId;

    @Column(name = "active", nullable = false)
    private boolean active;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(final String employeeCode) { this.employeeCode = employeeCode; }
    public String getFirstName() { return firstName; }
    public void setFirstName(final String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(final String lastName) { this.lastName = lastName; }
    public String getWorkEmail() { return workEmail; }
    public void setWorkEmail(final String workEmail) { this.workEmail = workEmail; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(final String departmentId) { this.departmentId = departmentId; }
    public String getDesignation() { return designation; }
    public void setDesignation(final String designation) { this.designation = designation; }
    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }
    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(final LocalDate joinDate) { this.joinDate = joinDate; }
    public BigDecimal getMonthlyBasicSalary() { return monthlyBasicSalary; }
    public void setMonthlyBasicSalary(final BigDecimal monthlyBasicSalary) { this.monthlyBasicSalary = monthlyBasicSalary; }
    public String getBankName() { return bankName; }
    public void setBankName(final String bankName) { this.bankName = bankName; }
    public String getBankAccountMasked() { return bankAccountMasked; }
    public void setBankAccountMasked(final String bankAccountMasked) { this.bankAccountMasked = bankAccountMasked; }
    public String getPanMasked() { return panMasked; }
    public void setPanMasked(final String panMasked) { this.panMasked = panMasked; }
    public String getUanMasked() { return uanMasked; }
    public void setUanMasked(final String uanMasked) { this.uanMasked = uanMasked; }
    public boolean isActive() { return active; }
    public void setActive(final boolean active) { this.active = active; }
    public String getUserAccountId() { return userAccountId; }
    public void setUserAccountId(final String userAccountId) { this.userAccountId = userAccountId; }
}
