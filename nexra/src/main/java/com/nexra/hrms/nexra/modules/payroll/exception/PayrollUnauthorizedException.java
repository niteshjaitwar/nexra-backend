package com.nexra.hrms.nexra.modules.payroll.exception;

/**
 * Thrown when authenticated payroll user context is missing.
 */
public class PayrollUnauthorizedException extends RuntimeException {

    public PayrollUnauthorizedException(final String message) {
        super(message);
    }
}
