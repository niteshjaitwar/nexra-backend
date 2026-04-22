package com.nexra.hrms.nexra.modules.hrms.recruitment.exception;

/**
 * Raised when a recruitment workflow violates a business rule.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public class RecruitmentBusinessException extends RuntimeException {

    public RecruitmentBusinessException(final String message) {
        super(message);
    }
}
