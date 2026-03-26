package com.nexra.hrms.nexra.modules.hrms.employee.exception;

/**
 * Thrown when user lacks permission for employee-core operations.
 */
public class EmployeeCoreForbiddenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmployeeCoreForbiddenException(final String message) {
        super(message);
    }
}
