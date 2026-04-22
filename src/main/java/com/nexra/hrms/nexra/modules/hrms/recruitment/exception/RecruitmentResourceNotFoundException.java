package com.nexra.hrms.nexra.modules.hrms.recruitment.exception;

/**
 * Raised when a recruitment resource cannot be found in the tenant scope.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public class RecruitmentResourceNotFoundException extends RuntimeException {

    public RecruitmentResourceNotFoundException(final String message) {
        super(message);
    }
}
