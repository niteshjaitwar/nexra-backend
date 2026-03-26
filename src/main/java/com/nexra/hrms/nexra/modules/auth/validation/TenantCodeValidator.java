package com.nexra.hrms.nexra.modules.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates tenant codes used across module boundaries and external integrations.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
public class TenantCodeValidator implements ConstraintValidator<TenantCode, String> {

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return value.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$");
    }
}
