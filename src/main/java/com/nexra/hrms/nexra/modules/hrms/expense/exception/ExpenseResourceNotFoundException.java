package com.nexra.hrms.nexra.modules.hrms.expense.exception;

/**
 * Thrown when a requested expense resource is not found.
 */
public class ExpenseResourceNotFoundException extends RuntimeException {

    public ExpenseResourceNotFoundException(final String message) {
        super(message);
    }
}

