package com.nexra.hrms.nexra.modules.crm.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "crm_activities")
public class CrmActivityEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "lead_id", length = 36)
    private String leadId;

    @Column(name = "contact_id", length = 36)
    private String contactId;

    @Column(name = "deal_id", length = 36)
    private String dealId;

    @Column(name = "activity_type", nullable = false, length = 40)
    private String activityType;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

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

    public String getLeadId() {
        return leadId;
    }

    public void setLeadId(final String leadId) {
        this.leadId = leadId;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(final String contactId) {
        this.contactId = contactId;
    }

    public String getDealId() {
        return dealId;
    }

    public void setDealId(final String dealId) {
        this.dealId = dealId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(final String activityType) {
        this.activityType = activityType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(final Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(final String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
}

