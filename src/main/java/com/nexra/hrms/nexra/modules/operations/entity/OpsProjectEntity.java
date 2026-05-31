package com.nexra.hrms.nexra.modules.operations.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "ops_projects")
public class OpsProjectEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "code", nullable = false, length = 60)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "owner_user_id", nullable = false, length = 36)
    private String ownerUserId;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "crm_deal_id", length = 36)
    private String crmDealId;

    @Column(name = "department_code", length = 60)
    private String departmentCode;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getCode() { return code; }
    public void setCode(final String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(final String description) { this.description = description; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(final String ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }
    public String getCrmDealId() { return crmDealId; }
    public void setCrmDealId(final String crmDealId) { this.crmDealId = crmDealId; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(final String departmentCode) { this.departmentCode = departmentCode; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(final LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(final LocalDate endDate) { this.endDate = endDate; }
}
