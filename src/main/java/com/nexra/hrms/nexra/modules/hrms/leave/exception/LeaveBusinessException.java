package com.nexra.hrms.nexra.modules.hrms.leave.exception;

/**
 * Thrown when a leave management business rule validation fails.
 */
public class LeaveBusinessException extends RuntimeException {

    public LeaveBusinessException(final String message) {
        super(message);
    }
}

