package com.nexra.hrms.nexra.modules.crm.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "crm_deals")
public class CrmDealEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "account_id", length = 36)
    private String accountId;

    @Column(name = "contact_id", length = 36)
    private String contactId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "stage", nullable = false, length = 40)
    private String stage;

    @Column(name = "value_amount", precision = 14, scale = 2)
    private BigDecimal valueAmount;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "owner_user_id", nullable = false, length = 36)
    private String ownerUserId;

    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

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

    public String getContactId() {
        return contactId;
    }

    public void setContactId(final String contactId) {
        this.contactId = contactId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(final String stage) {
        this.stage = stage;
    }

    public BigDecimal getValueAmount() {
        return valueAmount;
    }

    public void setValueAmount(final BigDecimal valueAmount) {
        this.valueAmount = valueAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(final String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public LocalDate getExpectedCloseDate() {
        return expectedCloseDate;
    }

    public void setExpectedCloseDate(final LocalDate expectedCloseDate) {
        this.expectedCloseDate = expectedCloseDate;
    }
}

