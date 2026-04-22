package com.nexra.hrms.nexra.modules.hrms.attendance.exception;

/**
 * Thrown when the request is missing a valid authenticated attendance user context.
 */
public class AttendanceUnauthorizedException extends RuntimeException {

    public AttendanceUnauthorizedException(final String message) {
        super(message);
    }
}

