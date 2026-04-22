package com.nexra.hrms.nexra.modules.hrms.attendance.exception;

/**
 * Thrown when a business rule validation fails in attendance workflows.
 */
public class AttendanceBusinessException extends RuntimeException {

    public AttendanceBusinessException(final String message) {
        super(message);
    }
}

