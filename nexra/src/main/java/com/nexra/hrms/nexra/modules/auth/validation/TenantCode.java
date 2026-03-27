package com.nexra.hrms.nexra.modules.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restricts tenant codes to DNS-safe characters for URLs, claims, and integration points.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Documented
@Constraint(validatedBy = TenantCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantCode {

    String message() default "Tenant code must contain only lowercase letters, digits, and hyphens.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
