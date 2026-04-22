package com.nexra.hrms.nexra.modules.hrms.expense.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseClaimItemView(
    String itemId, LocalDate expenseDate, String categoryCode, String description, BigDecimal amount, String receiptReference
) {
}

