package com.nexra.hrms.nexra.modules.crm.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import com.nexra.hrms.nexra.modules.crm.model.CrmLeadStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "crm_leads")
public class CrmLeadEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "full_name", nullable = false, length = 180)
    private String fullName;

    @Column(name = "email", nullable = false, length = 180)
    private String email;

    @Column(name = "phone", length = 40)
    private String phone;

    @Column(name = "company", nullable = false, length = 180)
    private String company;

    @Column(name = "source", length = 80)
    private String source;

    @Column(name = "owner_user_id", nullable = false, length = 36)
    private String ownerUserId;

    @Column(name = "notes", length = 4000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CrmLeadStatus status;

    @Column(name = "domain_created_at", nullable = false)
    private Instant domainCreatedAt;

    @Column(name = "domain_updated_at", nullable = false)
    private Instant domainUpdatedAt;

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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(final String company) {
        this.company = company;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(final String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public CrmLeadStatus getStatus() {
        return status;
    }

    public void setStatus(final CrmLeadStatus status) {
        this.status = status;
    }

    public Instant getDomainCreatedAt() {
        return domainCreatedAt;
    }

    public void setDomainCreatedAt(final Instant domainCreatedAt) {
        this.domainCreatedAt = domainCreatedAt;
    }

    public Instant getDomainUpdatedAt() {
        return domainUpdatedAt;
    }

    public void setDomainUpdatedAt(final Instant domainUpdatedAt) {
        this.domainUpdatedAt = domainUpdatedAt;
    }
}
