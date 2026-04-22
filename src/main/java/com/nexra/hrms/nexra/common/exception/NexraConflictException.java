package com.nexra.hrms.nexra.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an operation conflicts with the current state of the system,
 * such as duplicate keys or stale optimistic updates. Maps to HTTP 409 via
 * the common exception handler.
 *
 * @author niteshjaitwar
 */
public class NexraConflictException extends NexraException {

    private static final long serialVersionUID = 1L;

    /**
     * Builds a conflict exception with a user facing message.
     *
     * @param message human readable description safe to surface to clients.
     */
    public NexraConflictException(final String message) {
        super(HttpStatus.CONFLICT, "CONFLICT", message);
    }
}
