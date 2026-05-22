package com.nexra.hrms.nexra.modules.crm.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "crm_contacts")
public class CrmContactEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "account_id", length = 36)
    private String accountId;

    @Column(name = "full_name", nullable = false, length = 180)
    private String fullName;

    @Column(name = "email", length = 180)
    private String email;

    @Column(name = "phone", length = 40)
    private String phone;

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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(final String accountId) {
        this.accountId = accountId;
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

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(final String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
}

