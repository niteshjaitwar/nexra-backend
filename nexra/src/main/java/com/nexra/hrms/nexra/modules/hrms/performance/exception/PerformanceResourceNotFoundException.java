package com.nexra.hrms.nexra.modules.hrms.performance.exception;

/**
 * Raised when a performance resource cannot be found in the tenant scope.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public class PerformanceResourceNotFoundException extends RuntimeException {

    public PerformanceResourceNotFoundException(final String message) {
        super(message);
    }
}
