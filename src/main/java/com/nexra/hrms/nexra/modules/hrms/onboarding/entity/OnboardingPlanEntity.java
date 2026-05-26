package com.nexra.hrms.nexra.modules.hrms.onboarding.entity;

import com.nexra.hrms.nexra.modules.hrms.onboarding.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "onboarding_plans")
public class OnboardingPlanEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "plan_id", nullable = false, length = 36)
    private String planId;

    @Column(name = "tenant_code", nullable = false, length = 64)
    private String tenantCode;

    @Column(name = "employee_id", nullable = false, length = 36)
    private String employeeId;

    @Column(name = "plan_name", nullable = false, length = 200)
    private String planName;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(final String planId) {
        this.planId = planId;
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

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(final String planName) {
        this.planName = planName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }
}
