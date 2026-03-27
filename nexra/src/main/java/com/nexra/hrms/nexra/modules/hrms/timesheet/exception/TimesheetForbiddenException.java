package com.nexra.hrms.nexra.modules.hrms.timesheet.exception;

/**
 * Thrown when a user lacks permission for a timesheet operation.
 */
public class TimesheetForbiddenException extends RuntimeException {

    public TimesheetForbiddenException(final String message) {
        super(message);
    }
}

