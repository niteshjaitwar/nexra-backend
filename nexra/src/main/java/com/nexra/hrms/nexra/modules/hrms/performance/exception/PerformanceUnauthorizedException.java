package com.nexra.hrms.nexra.modules.hrms.performance.exception;

/**
 * Raised when a performance request does not carry a valid authenticated user.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public class PerformanceUnauthorizedException extends RuntimeException {

    public PerformanceUnauthorizedException(final String message) {
        super(message);
    }
}
