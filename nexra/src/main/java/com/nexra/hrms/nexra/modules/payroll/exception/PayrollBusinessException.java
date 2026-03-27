package com.nexra.hrms.nexra.modules.payroll.exception;

/**
 * Thrown when payroll business rule validation fails.
 */
public class PayrollBusinessException extends RuntimeException {

    public PayrollBusinessException(final String message) {
        super(message);
    }
}
