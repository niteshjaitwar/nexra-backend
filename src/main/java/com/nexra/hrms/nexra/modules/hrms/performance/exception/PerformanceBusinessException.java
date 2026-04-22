package com.nexra.hrms.nexra.modules.hrms.performance.exception;

/**
 * Raised when a performance workflow violates a business rule.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public class PerformanceBusinessException extends RuntimeException {

    public PerformanceBusinessException(final String message) {
        super(message);
    }
}
