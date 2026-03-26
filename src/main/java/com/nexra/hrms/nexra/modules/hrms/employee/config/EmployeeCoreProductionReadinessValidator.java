package com.nexra.hrms.nexra.modules.hrms.employee.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Fails fast when production configuration is unsafe.
 */
@Component
@RequiredArgsConstructor
public class EmployeeCoreProductionReadinessValidator implements ApplicationRunner {

    private final Environment environment;
    private final EmployeeCoreProperties employeeCoreProperties;

    @Override
    public void run(final ApplicationArguments args) {
        if (isNonProductionProfileActive()) {
            return;
        }

        String jwtSecret = employeeCoreProperties.getSecurity().getJwtSecret();
        assertCondition(jwtSecret != null && !jwtSecret.isBlank(), "AUTH_JWT_SECRET must be configured.");
        assertCondition(jwtSecret.length() >= 32, "AUTH_JWT_SECRET must be at least 32 characters.");
        assertCondition(hasText(environment.getProperty("EMPLOYEE_CORE_DB_URL")), "EMPLOYEE_CORE_DB_URL must be configured.");
        assertCondition(hasText(environment.getProperty("EMPLOYEE_CORE_DB_USERNAME")), "EMPLOYEE_CORE_DB_USERNAME must be configured.");
        assertCondition(hasText(environment.getProperty("EMPLOYEE_CORE_DB_PASSWORD")), "EMPLOYEE_CORE_DB_PASSWORD must be configured.");
    }

    private boolean isNonProductionProfileActive() {
        for (String activeProfile : environment.getActiveProfiles()) {
            if ("dev".equalsIgnoreCase(activeProfile)
                || "test".equalsIgnoreCase(activeProfile)
                || "e2e".equalsIgnoreCase(activeProfile)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasText(final String value) {
        return value != null && !value.isBlank();
    }

    private void assertCondition(final boolean condition, final String message) {
        if (!condition) {
            throw new IllegalStateException("Production configuration invalid: " + message);
        }
    }
}
