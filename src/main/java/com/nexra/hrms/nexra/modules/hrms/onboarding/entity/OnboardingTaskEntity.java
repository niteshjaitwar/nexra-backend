package com.nexra.hrms.nexra.modules.hrms.onboarding.entity;

import com.nexra.hrms.nexra.modules.hrms.onboarding.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "onboarding_tasks")
public class OnboardingTaskEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "task_id", nullable = false, length = 36)
    private String taskId;

    @Column(name = "tenant_code", nullable = false, length = 64)
    private String tenantCode;

    @Column(name = "plan_id", nullable = false, length = 36)
    private String planId;

    @Column(name = "task_name", nullable = false, length = 200)
    private String taskName;

    @Column(name = "owner_team", length = 80)
    private String ownerTeam;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(final String taskId) {
        this.taskId = taskId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(final String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(final String planId) {
        this.planId = planId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(final String taskName) {
        this.taskName = taskName;
    }

    public String getOwnerTeam() {
        return ownerTeam;
    }

    public void setOwnerTeam(final String ownerTeam) {
        this.ownerTeam = ownerTeam;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }
}
