package com.nexra.hrms.nexra.modules.hrms.timesheet.exception;

/**
 * Thrown when the authenticated timesheet user context is missing.
 */
public class TimesheetUnauthorizedException extends RuntimeException {

    public TimesheetUnauthorizedException(final String message) {
        super(message);
    }
}

