package com.nexra.hrms.nexra.modules.hrms.employee.exception;

/**
 * Thrown when authenticated employee-core user context is missing.
 */
public class EmployeeCoreUnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmployeeCoreUnauthorizedException(final String message) {
        super(message);
    }
}
