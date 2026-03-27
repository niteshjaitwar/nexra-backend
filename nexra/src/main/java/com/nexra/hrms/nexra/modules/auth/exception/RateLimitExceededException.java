package com.nexra.hrms.nexra.modules.auth.exception;

/**
 * Represents request throttling and temporary lock conditions.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public class RateLimitExceededException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates rate-limit exception with contextual message.
     *
     * @param message exception message
     */
    public RateLimitExceededException(final String message) {
        super(message);
    }
}
