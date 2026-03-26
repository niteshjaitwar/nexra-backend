package com.nexra.hrms.nexra.modules.hrms.employee.exception;

/**
 * Thrown when employee-core business rule validation fails.
 */
public class EmployeeCoreBusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmployeeCoreBusinessException(final String message) {
        super(message);
    }
}
