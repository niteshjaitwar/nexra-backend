package com.nexra.hrms.nexra.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Root unchecked exception for the Nexra platform. All domain exceptions
 * SHOULD extend this base so the common exception handler can produce a
 * uniform error envelope. Carries an HTTP status hint and a machine readable
 * error code used by API consumers for conditional handling.
 *
 * @author niteshjaitwar
 */
public class NexraException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final String errorCode;

    /**
     * Builds an exception with status, error code and message.
     *
     * @param status    HTTP status hint propagated by the exception handler.
     * @param errorCode machine readable error code surfaced under ApiResponse.code.
     * @param message   human readable description safe to surface to clients.
     */
    public NexraException(final HttpStatus status, final String errorCode, final String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    /**
     * Builds an exception with status, error code, message and root cause.
     *
     * @param status    HTTP status hint propagated by the exception handler.
     * @param errorCode machine readable error code surfaced under ApiResponse.code.
     * @param message   human readable description safe to surface to clients.
     * @param cause     originating cause.
     */
    public NexraException(final HttpStatus status, final String errorCode, final String message, final Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    /**
     * Returns the HTTP status hint associated with this exception.
     *
     * @return HTTP status.
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * Returns the machine readable error code.
     *
     * @return error code string.
     */
    public String getErrorCode() {
        return errorCode;
    }
}
