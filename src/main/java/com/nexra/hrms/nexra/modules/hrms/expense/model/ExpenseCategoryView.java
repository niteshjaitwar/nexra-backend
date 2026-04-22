package com.nexra.hrms.nexra.modules.hrms.expense.model;

import java.math.BigDecimal;
import java.time.Instant;

public record ExpenseCategoryView(
    String categoryId, String tenantCode, String code, String name, BigDecimal maxAmountPerClaim,
    boolean requiresReceipt, boolean active, Instant updatedAt, String updatedBy
) {
}

