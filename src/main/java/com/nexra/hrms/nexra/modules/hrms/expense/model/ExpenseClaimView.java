package com.nexra.hrms.nexra.modules.hrms.expense.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ExpenseClaimView(
    String claimId, String tenantCode, String employeeId, LocalDate claimDate, String title, String currency,
    BigDecimal totalAmount, String status, String approverUserId, String approverEmail, String approvalComment, Instant reimbursedAt,
    Instant createdAt, Instant updatedAt, List<ExpenseClaimItemView> items
) {
}

