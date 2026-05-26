package com.nexra.hrms.nexra.modules.crm.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "crm_custom_field_definitions")
public class CrmCustomFieldDefinitionEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "module_key", nullable = false, length = 60)
    private String moduleKey;

    @Column(name = "field_key", nullable = false, length = 80)
    private String fieldKey;

    @Column(name = "label", nullable = false, length = 120)
    private String label;

    @Column(name = "field_type", nullable = false, length = 40)
    private String fieldType;

    @Column(name = "required", nullable = false)
    private boolean required;

    @Column(name = "searchable", nullable = false)
    private boolean searchable;

    @Column(name = "options_json", columnDefinition = "TEXT")
    private String optionsJson;

    @Column(name = "validation_json", columnDefinition = "TEXT")
    private String validationJson;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getModuleKey() { return moduleKey; }
    public void setModuleKey(final String moduleKey) { this.moduleKey = moduleKey; }
    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(final String fieldKey) { this.fieldKey = fieldKey; }
    public String getLabel() { return label; }
    public void setLabel(final String label) { this.label = label; }
    public String getFieldType() { return fieldType; }
    public void setFieldType(final String fieldType) { this.fieldType = fieldType; }
    public boolean isRequired() { return required; }
    public void setRequired(final boolean required) { this.required = required; }
    public boolean isSearchable() { return searchable; }
    public void setSearchable(final boolean searchable) { this.searchable = searchable; }
    public String getOptionsJson() { return optionsJson; }
    public void setOptionsJson(final String optionsJson) { this.optionsJson = optionsJson; }
    public String getValidationJson() { return validationJson; }
    public void setValidationJson(final String validationJson) { this.validationJson = validationJson; }
    public boolean isActive() { return active; }
    public void setActive(final boolean active) { this.active = active; }
}
