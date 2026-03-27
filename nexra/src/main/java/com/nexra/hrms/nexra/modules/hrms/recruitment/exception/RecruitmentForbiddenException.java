package com.nexra.hrms.nexra.modules.hrms.recruitment.exception;

/**
 * Raised when an authenticated caller lacks recruitment module permission.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public class RecruitmentForbiddenException extends RuntimeException {

    public RecruitmentForbiddenException(final String message) {
        super(message);
    }
}
