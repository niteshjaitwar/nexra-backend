package com.nexra.hrms.nexra.modules.hrms.attendance.exception;

/**
 * Thrown when a requested attendance domain resource does not exist.
 */
public class AttendanceResourceNotFoundException extends RuntimeException {

    public AttendanceResourceNotFoundException(final String message) {
        super(message);
    }
}

