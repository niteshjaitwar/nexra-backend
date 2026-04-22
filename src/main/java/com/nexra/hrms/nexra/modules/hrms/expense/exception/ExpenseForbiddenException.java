package com.nexra.hrms.nexra.modules.hrms.expense.exception;

/**
 * Thrown when a user lacks permission for an expense operation.
 */
public class ExpenseForbiddenException extends RuntimeException {

    public ExpenseForbiddenException(final String message) {
        super(message);
    }
}

