package com.nexra.hrms.nexra.modules.crm.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "crm_quotes")
public class CrmQuoteEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "quote_number", nullable = false, length = 60)
    private String quoteNumber;

    @Column(name = "title", nullable = false, length = 240)
    private String title;

    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "currency", nullable = false, length = 4)
    private String currency;

    @Column(name = "deal_id", length = 36)
    private String dealId;

    @Column(name = "account_id", length = 36)
    private String accountId;

    @Column(name = "contact_id", length = 36)
    private String contactId;

    @Column(name = "owner_user_id", nullable = false, length = 36)
    private String ownerUserId;

    @Column(name = "subtotal", nullable = false, precision = 16, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_total", nullable = false, precision = 16, scale = 2)
    private BigDecimal discountTotal;

    @Column(name = "tax_total", nullable = false, precision = 16, scale = 2)
    private BigDecimal taxTotal;

    @Column(name = "grand_total", nullable = false, precision = 16, scale = 2)
    private BigDecimal grandTotal;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getQuoteNumber() { return quoteNumber; }
    public void setQuoteNumber(final String quoteNumber) { this.quoteNumber = quoteNumber; }
    public String getTitle() { return title; }
    public void setTitle(final String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(final String status) { this.status = status; }
    public String getCurrency() { return currency; }
    public void setCurrency(final String currency) { this.currency = currency; }
    public String getDealId() { return dealId; }
    public void setDealId(final String dealId) { this.dealId = dealId; }
    public String getAccountId() { return accountId; }
    public void setAccountId(final String accountId) { this.accountId = accountId; }
    public String getContactId() { return contactId; }
    public void setContactId(final String contactId) { this.contactId = contactId; }
    public String getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(final String ownerUserId) { this.ownerUserId = ownerUserId; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(final BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscountTotal() { return discountTotal; }
    public void setDiscountTotal(final BigDecimal discountTotal) { this.discountTotal = discountTotal; }
    public BigDecimal getTaxTotal() { return taxTotal; }
    public void setTaxTotal(final BigDecimal taxTotal) { this.taxTotal = taxTotal; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(final BigDecimal grandTotal) { this.grandTotal = grandTotal; }
    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(final LocalDate validUntil) { this.validUntil = validUntil; }
}
