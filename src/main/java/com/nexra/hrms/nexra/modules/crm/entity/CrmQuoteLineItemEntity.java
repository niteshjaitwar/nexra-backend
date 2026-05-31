package com.nexra.hrms.nexra.modules.crm.entity;

import com.nexra.hrms.nexra.common.persistence.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "crm_quote_line_items")
public class CrmQuoteLineItemEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "quote_id", nullable = false, length = 36)
    private String quoteId;

    @Column(name = "tenant_code", nullable = false, length = 60)
    private String tenantCode;

    @Column(name = "line_no", nullable = false)
    private int lineNo;

    @Column(name = "product_name", nullable = false, length = 240)
    private String productName;

    @Column(name = "quantity", nullable = false, precision = 14, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 16, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_percent", nullable = false, precision = 7, scale = 4)
    private BigDecimal discountPercent;

    @Column(name = "tax_percent", nullable = false, precision = 7, scale = 4)
    private BigDecimal taxPercent;

    @Column(name = "line_total", nullable = false, precision = 16, scale = 2)
    private BigDecimal lineTotal;

    public String getId() { return id; }
    public void setId(final String id) { this.id = id; }
    public String getQuoteId() { return quoteId; }
    public void setQuoteId(final String quoteId) { this.quoteId = quoteId; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public int getLineNo() { return lineNo; }
    public void setLineNo(final int lineNo) { this.lineNo = lineNo; }
    public String getProductName() { return productName; }
    public void setProductName(final String productName) { this.productName = productName; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(final BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(final BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(final BigDecimal discountPercent) { this.discountPercent = discountPercent; }
    public BigDecimal getTaxPercent() { return taxPercent; }
    public void setTaxPercent(final BigDecimal taxPercent) { this.taxPercent = taxPercent; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(final BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
