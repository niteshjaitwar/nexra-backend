package com.nexra.hrms.nexra.modules.hrms.timesheet.exception;

/**
 * Thrown when a requested timesheet resource is not found.
 */
public class TimesheetResourceNotFoundException extends RuntimeException {

    public TimesheetResourceNotFoundException(final String message) {
        super(message);
    }
}

