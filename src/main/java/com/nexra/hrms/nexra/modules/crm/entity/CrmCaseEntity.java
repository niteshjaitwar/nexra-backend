package com.nexra.hrms.nexra.modules.crm.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "crm_cases")
public class CrmCaseEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "subject", nullable = false, length = 240)
    private String subject;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "priority", nullable = false, length = 20)
    private String priority;

    @Column(name = "account_id", length = 36)
    private String accountId;

    @Column(name = "contact_id", length = 36)
    private String contactId;

    @Column(name = "owner_user_id", nullable = false, length = 36)
    private String ownerUserId;

    @Column(name = "sla_due_at")
    private Instant slaDueAt;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getSubject() { return subject; }
    public void setSubject(final String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(final String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(final String priority) { this.priority = priority; }
    public String getAccountId() { return accountId; }
    public void setAccountId(final String accountId) { this.accountId = accountId; }
    public String getContactId() { return contactId; }
    public void setContactId(final String contactId) { this.contactId = contactId; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(final String ownerUserId) { this.ownerUserId = ownerUserId; }
    public Instant getSlaDueAt() { return slaDueAt; }
    public void setSlaDueAt(final Instant slaDueAt) { this.slaDueAt = slaDueAt; }
}
