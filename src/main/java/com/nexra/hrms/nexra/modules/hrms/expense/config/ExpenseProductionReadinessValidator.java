package com.nexra.hrms.nexra.modules.hrms.expense.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Fails startup when expense production configuration is unsafe.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class ExpenseProductionReadinessValidator implements ApplicationRunner {

    private static final int MIN_SECRET_LENGTH = 32;

    private final Environment environment;
    private final ExpenseProperties properties;

    @Override
    public void run(final ApplicationArguments args) {
        if (isNonProductionProfileActive()) {
            return;
        }
        assertCondition(hasText(environment.getProperty("spring.datasource.url")),
                "spring.datasource.url must be configured.");
        assertCondition(!environment.getProperty("spring.datasource.url", "").startsWith("jdbc:h2:"),
                "spring.datasource.url must not use H2 in prod.");
        assertCondition(hasText(environment.getProperty("spring.datasource.username")),
                "spring.datasource.username must be configured.");
        assertCondition(hasText(environment.getProperty("spring.datasource.password")),
                "spring.datasource.password must be configured.");
        assertCondition(properties.getSecurity().getJwtSecret() != null
                        && properties.getSecurity().getJwtSecret().trim().length() >= MIN_SECRET_LENGTH,
                "app.expense.security.jwt-secret must be at least 32 characters in production.");
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
