package com.nexra.hrms.nexra.modules.hrms.expense.entity;

import com.nexra.hrms.nexra.modules.hrms.expense.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ex_claim_items")
public class ExpenseClaimItemEntity extends AbstractAuditableEntity {
    @Id @Column(name = "id", nullable = false, length = 36) private String id;
    @Column(name = "claim_id", nullable = false, length = 36) private String claimId;
    @Column(name = "tenant_code", nullable = false, length = 60) private String tenantCode;
    @Column(name = "expense_date", nullable = false) private LocalDate expenseDate;
    @Column(name = "category_code", nullable = false, length = 40) private String categoryCode;
    @Column(name = "description_text", nullable = false, length = 300) private String descriptionText;
    @Column(name = "amount", nullable = false, precision = 14, scale = 2) private BigDecimal amount;
    @Column(name = "receipt_reference", length = 200) private String receiptReference;
    public String getId() { return id; } public void setId(final String id) { this.id = id; }
    public String getClaimId() { return claimId; } public void setClaimId(final String claimId) { this.claimId = claimId; }
    public String getTenantCode() { return tenantCode; } public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public LocalDate getExpenseDate() { return expenseDate; } public void setExpenseDate(final LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getCategoryCode() { return categoryCode; } public void setCategoryCode(final String categoryCode) { this.categoryCode = categoryCode; }
    public String getDescriptionText() { return descriptionText; } public void setDescriptionText(final String descriptionText) { this.descriptionText = descriptionText; }
    public BigDecimal getAmount() { return amount; } public void setAmount(final BigDecimal amount) { this.amount = amount; }
    public String getReceiptReference() { return receiptReference; } public void setReceiptReference(final String receiptReference) { this.receiptReference = receiptReference; }
}

