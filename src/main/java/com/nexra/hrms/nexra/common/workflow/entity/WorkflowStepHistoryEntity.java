package com.nexra.hrms.nexra.common.workflow.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Append-only history of every workflow step transition (start, approve, reject,
 * escalate, complete). Provides a compliance-grade audit trail per instance.
 */
@Entity
@Table(name = "workflow_step_history")
public class WorkflowStepHistoryEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "instance_id", nullable = false, length = 36)
    private String instanceId;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "step_index", nullable = false)
    private int stepIndex;

    @Column(name = "step_name", nullable = false, length = 120)
    private String stepName;

    @Column(name = "action", nullable = false, length = 40)
    private String action;

    @Column(name = "actor_email", length = 160)
    private String actorEmail;

    @Column(name = "notes", length = 2000)
    private String notes;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getInstanceId() { return instanceId; }
    public void setInstanceId(final String instanceId) { this.instanceId = instanceId; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public int getStepIndex() { return stepIndex; }
    public void setStepIndex(final int stepIndex) { this.stepIndex = stepIndex; }
    public String getStepName() { return stepName; }
    public void setStepName(final String stepName) { this.stepName = stepName; }
    public String getAction() { return action; }
    public void setAction(final String action) { this.action = action; }
    public String getActorEmail() { return actorEmail; }
    public void setActorEmail(final String actorEmail) { this.actorEmail = actorEmail; }
    public String getNotes() { return notes; }
    public void setNotes(final String notes) { this.notes = notes; }
}
