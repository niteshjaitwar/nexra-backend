package com.nexra.hrms.nexra.modules.hrms.recruitment.exception;

/**
 * Raised when a recruitment request does not carry a valid authenticated user.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public class RecruitmentUnauthorizedException extends RuntimeException {

    public RecruitmentUnauthorizedException(final String message) {
        super(message);
    }
}
