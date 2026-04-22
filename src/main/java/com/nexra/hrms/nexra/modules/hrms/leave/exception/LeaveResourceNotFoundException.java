package com.nexra.hrms.nexra.modules.hrms.leave.exception;

/**
 * Thrown when a leave management resource is not found.
 */
public class LeaveResourceNotFoundException extends RuntimeException {

    public LeaveResourceNotFoundException(final String message) {
        super(message);
    }
}

