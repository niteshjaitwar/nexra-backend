package com.nexra.hrms.nexra.modules.hrms.leave.exception;

/**
 * Thrown when authenticated leave user context is missing.
 */
public class LeaveUnauthorizedException extends RuntimeException {

    public LeaveUnauthorizedException(final String message) {
        super(message);
    }
}

