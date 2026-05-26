package com.nexra.hrms.nexra.modules.crm.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "crm_record_sharing_rules")
public class CrmRecordSharingRuleEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "module_key", nullable = false, length = 60)
    private String moduleKey;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Lob
    @Column(name = "criteria_json", nullable = false)
    private String criteriaJson;

    @Column(name = "principal_type", nullable = false, length = 40)
    private String principalType;

    @Column(name = "principal_key", nullable = false, length = 120)
    private String principalKey;

    @Column(name = "access_level", nullable = false, length = 30)
    private String accessLevel;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getModuleKey() { return moduleKey; }
    public void setModuleKey(final String moduleKey) { this.moduleKey = moduleKey; }
    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }
    public String getCriteriaJson() { return criteriaJson; }
    public void setCriteriaJson(final String criteriaJson) { this.criteriaJson = criteriaJson; }
    public String getPrincipalType() { return principalType; }
    public void setPrincipalType(final String principalType) { this.principalType = principalType; }
    public String getPrincipalKey() { return principalKey; }
    public void setPrincipalKey(final String principalKey) { this.principalKey = principalKey; }
    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(final String accessLevel) { this.accessLevel = accessLevel; }
    public boolean isActive() { return active; }
    public void setActive(final boolean active) { this.active = active; }
}
