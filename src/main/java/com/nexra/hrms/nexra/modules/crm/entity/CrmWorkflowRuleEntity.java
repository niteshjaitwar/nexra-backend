package com.nexra.hrms.nexra.modules.crm.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "crm_workflow_rules")
public class CrmWorkflowRuleEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "module_key", nullable = false, length = 60)
    private String moduleKey;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "trigger_event", nullable = false, length = 60)
    private String triggerEvent;

    @Column(name = "criteria_json", nullable = false, columnDefinition = "TEXT")
    private String criteriaJson;

    @Column(name = "actions_json", nullable = false, columnDefinition = "TEXT")
    private String actionsJson;

    @Column(name = "priority", nullable = false)
    private int priority = 100;

    @Column(name = "active", nullable = false)
    private boolean active;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getModuleKey() { return moduleKey; }
    public void setModuleKey(final String moduleKey) { this.moduleKey = moduleKey; }
    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }
    public String getTriggerEvent() { return triggerEvent; }
    public void setTriggerEvent(final String triggerEvent) { this.triggerEvent = triggerEvent; }
    public String getCriteriaJson() { return criteriaJson; }
    public void setCriteriaJson(final String criteriaJson) { this.criteriaJson = criteriaJson; }
    public String getActionsJson() { return actionsJson; }
    public void setActionsJson(final String actionsJson) { this.actionsJson = actionsJson; }
    public int getPriority() { return priority; }
    public void setPriority(final int priority) { this.priority = priority; }
    public boolean isActive() { return active; }
    public void setActive(final boolean active) { this.active = active; }
}
