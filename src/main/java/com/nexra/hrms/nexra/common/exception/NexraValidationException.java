package com.nexra.hrms.nexra.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a request violates a business rule that is not covered by bean
 * validation. Maps to HTTP 422 via the common exception handler.
 *
 * @author niteshjaitwar
 */
public class NexraValidationException extends NexraException {

    private static final long serialVersionUID = 1L;

    /**
     * Builds a business validation exception with a user facing message.
     *
     * @param message human readable description safe to surface to clients.
     */
    public NexraValidationException(final String message) {
        super(HttpStatus.valueOf(422), "BUSINESS_RULE_VIOLATION", message);
    }
}
