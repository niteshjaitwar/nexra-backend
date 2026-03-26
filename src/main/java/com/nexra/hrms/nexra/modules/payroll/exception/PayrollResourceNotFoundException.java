package com.nexra.hrms.nexra.modules.payroll.exception;

/**
 * Thrown when a payroll resource is not found.
 */
public class PayrollResourceNotFoundException extends RuntimeException {

    public PayrollResourceNotFoundException(final String message) {
        super(message);
    }
}
