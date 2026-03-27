package com.nexra.hrms.nexra.modules.hrms.employee.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Enforces a narrow tenant-code format for stable multi-tenant identifiers.
 */
public class TenantCodeValidator implements ConstraintValidator<TenantCode, String> {

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return value.matches("^[A-Za-z0-9_-]{2,60}$");
    }
}
