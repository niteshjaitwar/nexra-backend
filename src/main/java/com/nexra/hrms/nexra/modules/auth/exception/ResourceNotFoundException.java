package com.nexra.hrms.nexra.modules.auth.exception;

/**
 * Represents missing resource errors in service workflows.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates resource-not-found exception with contextual message.
     *
     * @param message exception message
     */
    public ResourceNotFoundException(final String message) {
        super(message);
    }
}
