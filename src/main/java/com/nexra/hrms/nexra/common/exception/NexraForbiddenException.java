package com.nexra.hrms.nexra.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a principal is authenticated but lacks permission to access the
 * requested resource. Maps to HTTP 403 via the common exception handler.
 *
 * @author niteshjaitwar
 */
public class NexraForbiddenException extends NexraException {

    private static final long serialVersionUID = 1L;

    /**
     * Builds a forbidden exception with a user facing message.
     *
     * @param message human readable description safe to surface to clients.
     */
    public NexraForbiddenException(final String message) {
        super(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }
}
