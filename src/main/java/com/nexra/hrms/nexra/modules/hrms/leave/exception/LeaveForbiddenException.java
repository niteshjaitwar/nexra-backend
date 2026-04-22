package com.nexra.hrms.nexra.modules.hrms.leave.exception;

/**
 * Thrown when a user lacks permission for a leave management operation.
 */
public class LeaveForbiddenException extends RuntimeException {

    public LeaveForbiddenException(final String message) {
        super(message);
    }
}

