package com.nexra.hrms.nexra.modules.payroll.exception;

/**
 * Thrown when user lacks permission for payroll operations.
 */
public class PayrollForbiddenException extends RuntimeException {

    public PayrollForbiddenException(final String message) {
        super(message);
    }
}
