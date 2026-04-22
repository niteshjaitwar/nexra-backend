package com.nexra.hrms.nexra.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource does not exist. Maps to HTTP 404 via the
 * common exception handler.
 *
 * @author niteshjaitwar
 */
public class NexraNotFoundException extends NexraException {

    private static final long serialVersionUID = 1L;

    /**
     * Builds a not found exception with a user facing message.
     *
     * @param message human readable description safe to surface to clients.
     */
    public NexraNotFoundException(final String message) {
        super(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }
}
