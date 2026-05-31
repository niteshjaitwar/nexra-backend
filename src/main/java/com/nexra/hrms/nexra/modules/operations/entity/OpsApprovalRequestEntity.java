package com.nexra.hrms.nexra.modules.operations.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ops_approval_requests")
public class OpsApprovalRequestEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "reference_type", nullable = false, length = 60)
    private String referenceType;

    @Column(name = "reference_id", nullable = false, length = 36)
    private String referenceId;

    @Column(name = "requested_by_user_id", nullable = false, length = 36)
    private String requestedByUserId;

    @Column(name = "approver_user_id", length = 36)
    private String approverUserId;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "sla_due_at")
    private Instant slaDueAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "workflow_instance_id", length = 36)
    private String workflowInstanceId;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(final String referenceType) { this.referenceType = referenceType; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(final String referenceId) { this.referenceId = referenceId; }
    public String getRequestedByUserId() { return requestedByUserId; }
    public void setRequestedByUserId(final String requestedByUserId) { this.requestedByUserId = requestedByUserId; }
    public String getApproverUserId() { return approverUserId; }
    public void setApproverUserId(final String approverUserId) { this.approverUserId = approverUserId; }
    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(final String notes) { this.notes = notes; }
    public Instant getSlaDueAt() { return slaDueAt; }
    public void setSlaDueAt(final Instant slaDueAt) { this.slaDueAt = slaDueAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(final Instant decidedAt) { this.decidedAt = decidedAt; }
    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public void setWorkflowInstanceId(final String workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }
}
