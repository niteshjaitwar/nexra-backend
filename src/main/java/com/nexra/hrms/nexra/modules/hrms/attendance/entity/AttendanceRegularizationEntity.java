package com.nexra.hrms.nexra.modules.hrms.attendance.entity;

import com.nexra.hrms.nexra.modules.hrms.attendance.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "at_regularization_requests")
public class AttendanceRegularizationEntity extends AbstractAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;
    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;
    @Column(name = "employee_id", nullable = false, length = 36)
    private String employeeId;
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;
    @Column(name = "reason", length = 500)
    private String reason;
    @Column(name = "requested_check_in_at")
    private Instant requestedCheckInAt;
    @Column(name = "requested_check_out_at")
    private Instant requestedCheckOutAt;
    @Column(name = "status", nullable = false, length = 30)
    private String status;
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
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(final LocalDate workDate) { this.workDate = workDate; }
    public String getReason() { return reason; }
    public void setReason(final String reason) { this.reason = reason; }
    public Instant getRequestedCheckInAt() { return requestedCheckInAt; }
    public void setRequestedCheckInAt(final Instant requestedCheckInAt) { this.requestedCheckInAt = requestedCheckInAt; }
    public Instant getRequestedCheckOutAt() { return requestedCheckOutAt; }
    public void setRequestedCheckOutAt(final Instant requestedCheckOutAt) { this.requestedCheckOutAt = requestedCheckOutAt; }
    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }
    public String getApproverEmail() { return approverEmail; }
    public void setApproverEmail(final String approverEmail) { this.approverEmail = approverEmail; }
    public String getDecisionComment() { return decisionComment; }
    public void setDecisionComment(final String decisionComment) { this.decisionComment = decisionComment; }
    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public void setWorkflowInstanceId(final String workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }
}
