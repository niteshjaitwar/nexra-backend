package com.nexra.hrms.nexra.modules.operations.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ops_tasks")
public class OpsTaskEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "project_id", nullable = false, length = 36)
    private String projectId;

    @Column(name = "parent_task_id", length = 36)
    private String parentTaskId;

    @Column(name = "title", nullable = false, length = 240)
    private String title;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "assignee_user_id", length = 36)
    private String assigneeUserId;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "priority", nullable = false, length = 20)
    private String priority;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "estimate_hours", precision = 10, scale = 2)
    private BigDecimal estimateHours;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getProjectId() { return projectId; }
    public void setProjectId(final String projectId) { this.projectId = projectId; }
    public String getParentTaskId() { return parentTaskId; }
    public void setParentTaskId(final String parentTaskId) { this.parentTaskId = parentTaskId; }
    public String getTitle() { return title; }
    public void setTitle(final String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(final String description) { this.description = description; }
    public String getAssigneeUserId() { return assigneeUserId; }
    public void setAssigneeUserId(final String assigneeUserId) { this.assigneeUserId = assigneeUserId; }
    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(final String priority) { this.priority = priority; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(final LocalDate dueDate) { this.dueDate = dueDate; }
    public BigDecimal getEstimateHours() { return estimateHours; }
    public void setEstimateHours(final BigDecimal estimateHours) { this.estimateHours = estimateHours; }
}
