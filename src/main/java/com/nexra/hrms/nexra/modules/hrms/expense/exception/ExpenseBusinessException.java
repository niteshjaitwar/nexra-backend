package com.nexra.hrms.nexra.modules.hrms.expense.exception;

/**
 * Thrown when an expense business rule validation fails.
 */
public class ExpenseBusinessException extends RuntimeException {

    public ExpenseBusinessException(final String message) {
        super(message);
    }
}

