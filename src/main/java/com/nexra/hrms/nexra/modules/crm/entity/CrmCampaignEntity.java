package com.nexra.hrms.nexra.modules.crm.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "crm_campaigns")
public class CrmCampaignEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "name", nullable = false, length = 240)
    private String name;

    @Column(name = "campaign_type", nullable = false, length = 40)
    private String campaignType;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "budget", precision = 16, scale = 2)
    private BigDecimal budget;

    @Column(name = "actual_cost", precision = 16, scale = 2)
    private BigDecimal actualCost;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "owner_user_id", nullable = false, length = 36)
    private String ownerUserId;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getName() { return name; }
    public void setName(final String name) { this.name = name; }
    public String getCampaignType() { return campaignType; }
    public void setCampaignType(final String campaignType) { this.campaignType = campaignType; }
    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(final String description) { this.description = description; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(final BigDecimal budget) { this.budget = budget; }
    public BigDecimal getActualCost() { return actualCost; }
    public void setActualCost(final BigDecimal actualCost) { this.actualCost = actualCost; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(final LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(final LocalDate endDate) { this.endDate = endDate; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(final String ownerUserId) { this.ownerUserId = ownerUserId; }
}
