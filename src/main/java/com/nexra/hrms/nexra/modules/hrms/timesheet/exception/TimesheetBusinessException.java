package com.nexra.hrms.nexra.modules.hrms.timesheet.exception;

/**
 * Thrown when a timesheet business rule validation fails.
 */
public class TimesheetBusinessException extends RuntimeException {

    public TimesheetBusinessException(final String message) {
        super(message);
    }
}

