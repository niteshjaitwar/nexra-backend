package com.nexra.hrms.nexra.modules.auth.exception;

/**
 * Represents domain-level business rule violations.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates business exception with contextual message.
     *
     * @param message exception message
     */
    public BusinessException(final String message) {
        super(message);
    }
}
