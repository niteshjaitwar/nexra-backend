package com.nexra.hrms.nexra.modules.hrms.attendance.exception;

/**
 * Thrown when an authenticated user lacks permission for an attendance operation.
 */
public class AttendanceForbiddenException extends RuntimeException {

    public AttendanceForbiddenException(final String message) {
        super(message);
    }
}

