package com.nexra.hrms.nexra.modules.hrms.performance.exception;

/**
 * Raised when an authenticated caller lacks performance module permission.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public class PerformanceForbiddenException extends RuntimeException {

    public PerformanceForbiddenException(final String message) {
        super(message);
    }
}
