package com.nexra.hrms.nexra.modules.hrms.timesheet.entity;

import com.nexra.hrms.nexra.modules.hrms.timesheet.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ts_entries")
public class TimesheetEntryEntity extends AbstractAuditableEntity {
    @Id @Column(name = "id", nullable = false, length = 36) private String id;
    @Column(name = "tenant_code", nullable = false, length = 60) private String tenantCode;
    @Column(name = "employee_id", nullable = false, length = 36) private String employeeId;
    @Column(name = "work_date", nullable = false) private LocalDate workDate;
    @Column(name = "project_code", nullable = false, length = 60) private String projectCode;
    @Column(name = "task_name", nullable = false, length = 160) private String taskName;
    @Column(name = "hours", nullable = false, precision = 10, scale = 2) private BigDecimal hours;
    @Column(name = "billable", nullable = false) private boolean billable;
    @Column(name = "status", nullable = false, length = 30) private String status;
    @Column(name = "approver_user_id", length = 36) private String approverUserId;
    @Column(name = "approver_email", length = 160) private String approverEmail;
    @Column(name = "approval_comment", length = 500) private String approvalComment;
    @Column(name = "notes", length = 500) private String notes;
    public String getId() { return id; } public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; } public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getEmployeeId() { return employeeId; } public void setEmployeeId(final String employeeId) { this.employeeId = employeeId; }
    public LocalDate getWorkDate() { return workDate; } public void setWorkDate(final LocalDate workDate) { this.workDate = workDate; }
    public String getProjectCode() { return projectCode; } public void setProjectCode(final String projectCode) { this.projectCode = projectCode; }
    public String getTaskName() { return taskName; } public void setTaskName(final String taskName) { this.taskName = taskName; }
    public BigDecimal getHours() { return hours; } public void setHours(final BigDecimal hours) { this.hours = hours; }
    public boolean isBillable() { return billable; } public void setBillable(final boolean billable) { this.billable = billable; }
    public String getStatus() { return status; } public void setStatus(final String status) { this.status = status; }
    public String getApproverUserId() { return approverUserId; } public void setApproverUserId(final String approverUserId) { this.approverUserId = approverUserId; }
    public String getApproverEmail() { return approverEmail; } public void setApproverEmail(final String approverEmail) { this.approverEmail = approverEmail; }
    public String getApprovalComment() { return approvalComment; } public void setApprovalComment(final String approvalComment) { this.approvalComment = approvalComment; }
    public String getNotes() { return notes; } public void setNotes(final String notes) { this.notes = notes; }
}

