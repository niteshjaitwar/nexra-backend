package com.nexra.hrms.nexra.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a caller is not authenticated for the requested operation.
 * Maps to HTTP 401 via the common exception handler.
 *
 * @author niteshjaitwar
 */
public class NexraUnauthorizedException extends NexraException {

    private static final long serialVersionUID = 1L;

    /**
     * Builds an unauthorized exception with a user-facing message.
     *
     * @param message human readable description safe to surface to clients.
     */
    public NexraUnauthorizedException(final String message) {
        super(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", message);
    }
}
