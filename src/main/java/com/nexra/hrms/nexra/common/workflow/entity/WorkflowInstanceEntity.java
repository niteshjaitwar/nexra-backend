package com.nexra.hrms.nexra.common.workflow.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "workflow_instances")
public class WorkflowInstanceEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "product_code", nullable = false, length = 40)
    private String productCode;

    @Column(name = "module_key", nullable = false, length = 80)
    private String moduleKey;

    @Column(name = "definition_id", length = 36)
    private String definitionId;

    @Column(name = "trigger_event", nullable = false, length = 80)
    private String triggerEvent;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "actor_email", length = 160)
    private String actorEmail;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "current_step_index", nullable = false)
    private int currentStepIndex = 0;

    @Column(name = "current_step_name", length = 120)
    private String currentStepName;

    @Column(name = "step_status", length = 40)
    private String stepStatus;

    @Column(name = "sla_due_at")
    private Instant slaDueAt;

    @Column(name = "escalated", nullable = false)
    private boolean escalated = false;

    @Column(name = "escalated_at")
    private Instant escalatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getProductCode() { return productCode; }
    public void setProductCode(final String productCode) { this.productCode = productCode; }
    public String getModuleKey() { return moduleKey; }
    public void setModuleKey(final String moduleKey) { this.moduleKey = moduleKey; }
    public String getDefinitionId() { return definitionId; }
    public void setDefinitionId(final String definitionId) { this.definitionId = definitionId; }
    public String getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(final String triggerEvent) { this.triggerEvent = triggerEvent; }
    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }
    public String getActorEmail() { return actorEmail; }
    public void setActorEmail(final String actorEmail) { this.actorEmail = actorEmail; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(final String payloadJson) { this.payloadJson = payloadJson; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(final String resultJson) { this.resultJson = resultJson; }
    public int getCurrentStepIndex() { return currentStepIndex; }
    public void setCurrentStepIndex(final int currentStepIndex) { this.currentStepIndex = currentStepIndex; }
    public String getCurrentStepName() { return currentStepName; }
    public void setCurrentStepName(final String currentStepName) { this.currentStepName = currentStepName; }
    public String getStepStatus() { return stepStatus; }
    public void setStepStatus(final String stepStatus) { this.stepStatus = stepStatus; }
    public Instant getSlaDueAt() { return slaDueAt; }
    public void setSlaDueAt(final Instant slaDueAt) { this.slaDueAt = slaDueAt; }
    public boolean isEscalated() { return escalated; }
    public void setEscalated(final boolean escalated) { this.escalated = escalated; }
    public Instant getEscalatedAt() { return escalatedAt; }
    public void setEscalatedAt(final Instant escalatedAt) { this.escalatedAt = escalatedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(final Instant completedAt) { this.completedAt = completedAt; }
}
