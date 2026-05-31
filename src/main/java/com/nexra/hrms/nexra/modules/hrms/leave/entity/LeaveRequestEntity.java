package com.nexra.hrms.nexra.modules.hrms.leave.entity;

import com.nexra.hrms.nexra.modules.hrms.leave.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lv_leave_requests")
public class LeaveRequestEntity extends AbstractAuditableEntity {
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;
    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;
    @Column(name = "employee_id", nullable = false, length = 36)
    private String employeeId;
    @Column(name = "leave_type_code", nullable = false, length = 40)
    private String leaveTypeCode;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    @Column(name = "total_days", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDays;
    @Column(name = "reason", length = 500)
    private String reason;
    @Column(name = "status", nullable = false, length = 30)
    private String status;
    @Column(name = "approver_user_id", length = 36)
    private String approverUserId;
    @Column(name = "approver_email", length = 160)
    private String approverEmail;
    @Column(name = "decision_comment", length = 500)
    private String decisionComment;
    @Column(name = "workflow_instance_id", length = 36)
    private String workflowInstanceId;
    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(final String employeeId) { this.employeeId = employeeId; }
    public String getLeaveTypeCode() { return leaveTypeCode; }
    public void setLeaveTypeCode(final String leaveTypeCode) { this.leaveTypeCode = leaveTypeCode; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(final LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(final LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getTotalDays() { return totalDays; }
    public void setTotalDays(final BigDecimal totalDays) { this.totalDays = totalDays; }
    public String getReason() { return reason; }
    public void setReason(final String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }
    public String getApproverUserId() { return approverUserId; }
    public void setApproverUserId(final String approverUserId) { this.approverUserId = approverUserId; }
    public String getApproverEmail() { return approverEmail; }
    public void setApproverEmail(final String approverEmail) { this.approverEmail = approverEmail; }
    public String getDecisionComment() { return decisionComment; }
    public void setDecisionComment(final String decisionComment) { this.decisionComment = decisionComment; }
    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public void setWorkflowInstanceId(final String workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }
}

