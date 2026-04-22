package com.nexra.hrms.nexra.modules.hrms.expense.exception;

/**
 * Thrown when the authenticated expense user context is missing.
 */
public class ExpenseUnauthorizedException extends RuntimeException {

    public ExpenseUnauthorizedException(final String message) {
        super(message);
    }
}

