package com.nexra.hrms.nexra.modules.crm.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "crm_accounts")
public class CrmAccountEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "name", nullable = false, length = 180)
    private String name;

    @Column(name = "website", length = 200)
    private String website;

    @Column(name = "industry", length = 80)
    private String industry;

    @Column(name = "owner_user_id", nullable = false, length = 36)
    private String ownerUserId;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(final String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(final String website) {
        this.website = website;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(final String industry) {
        this.industry = industry;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(final String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
}

