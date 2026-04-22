package com.nexra.hrms.nexra.modules.hrms.timesheet.entity;

import com.nexra.hrms.nexra.modules.hrms.timesheet.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ts_projects")
public class ProjectEntity extends AbstractAuditableEntity {
    @Id @Column(name = "id", nullable = false, length = 36) private String id;
    @Column(name = "tenant_code", nullable = false, length = 60) private String tenantCode;
    @Column(name = "project_code", nullable = false, length = 60) private String projectCode;
    @Column(name = "project_name", nullable = false, length = 160) private String projectName;
    @Column(name = "client_name", length = 160) private String clientName;
    @Column(name = "billable", nullable = false) private boolean billable;
    @Column(name = "active", nullable = false) private boolean active;
    public String getId() { return id; } public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; } public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getProjectCode() { return projectCode; } public void setProjectCode(final String projectCode) { this.projectCode = projectCode; }
    public String getProjectName() { return projectName; } public void setProjectName(final String projectName) { this.projectName = projectName; }
    public String getClientName() { return clientName; } public void setClientName(final String clientName) { this.clientName = clientName; }
    public boolean isBillable() { return billable; } public void setBillable(final boolean billable) { this.billable = billable; }
    public boolean isActive() { return active; } public void setActive(final boolean active) { this.active = active; }
}

