package com.nexra.hrms.nexra.modules.auth.exception;

/**
 * Represents authorization or authentication failures.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public class UnauthorizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates unauthorized exception with contextual message.
     *
     * @param message exception message
     */
    public UnauthorizedException(final String message) {
        super(message);
    }
}
