package com.nexra.hrms.nexra.modules.hrms.employee.exception;

/**
 * Thrown when an employee-core resource is not found.
 */
public class EmployeeCoreResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmployeeCoreResourceNotFoundException(final String message) {
        super(message);
    }
}
