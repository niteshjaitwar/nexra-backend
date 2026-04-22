package com.nexra.hrms.nexra.modules.hrms.expense.entity;

import com.nexra.hrms.nexra.modules.hrms.expense.entity.base.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "ex_claims")
public class ExpenseClaimEntity extends AbstractAuditableEntity {
    @Id @Column(name = "id", nullable = false, length = 36) private String id;
    @Column(name = "tenant_code", nullable = false, length = 60) private String tenantCode;
    @Column(name = "employee_id", nullable = false, length = 36) private String employeeId;
    @Column(name = "claim_date", nullable = false) private LocalDate claimDate;
    @Column(name = "title", nullable = false, length = 200) private String title;
    @Column(name = "currency", nullable = false, length = 12) private String currency;
    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2) private BigDecimal totalAmount;
    @Column(name = "status", nullable = false, length = 30) private String status;
    @Column(name = "approver_user_id", length = 36) private String approverUserId;
    @Column(name = "approver_email", length = 160) private String approverEmail;
    @Column(name = "approval_comment", length = 500) private String approvalComment;
    @Column(name = "reimbursed_at") private Instant reimbursedAt;
    public String getId() { return id; } public void setId(final String id) { this.id = id; }
    public String getTenantCode() { return tenantCode; } public void setTenantCode(final String tenantCode) { this.tenantCode = tenantCode; }
    public String getEmployeeId() { return employeeId; } public void setEmployeeId(final String employeeId) { this.employeeId = employeeId; }
    public LocalDate getClaimDate() { return claimDate; } public void setClaimDate(final LocalDate claimDate) { this.claimDate = claimDate; }
    public String getTitle() { return title; } public void setTitle(final String title) { this.title = title; }
    public String getCurrency() { return currency; } public void setCurrency(final String currency) { this.currency = currency; }
    public BigDecimal getTotalAmount() { return totalAmount; } public void setTotalAmount(final BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; } public void setStatus(final String status) { this.status = status; }
    public String getApproverUserId() { return approverUserId; } public void setApproverUserId(final String approverUserId) { this.approverUserId = approverUserId; }
    public String getApproverEmail() { return approverEmail; } public void setApproverEmail(final String approverEmail) { this.approverEmail = approverEmail; }
    public String getApprovalComment() { return approvalComment; } public void setApprovalComment(final String approvalComment) { this.approvalComment = approvalComment; }
    public Instant getReimbursedAt() { return reimbursedAt; } public void setReimbursedAt(final Instant reimbursedAt) { this.reimbursedAt = reimbursedAt; }
}

