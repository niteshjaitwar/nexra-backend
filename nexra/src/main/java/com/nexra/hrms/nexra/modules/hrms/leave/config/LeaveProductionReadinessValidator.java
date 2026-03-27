package com.nexra.hrms.nexra.modules.hrms.leave.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Fails fast when leave production configuration is unsafe.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class LeaveProductionReadinessValidator implements ApplicationRunner {

    private final Environment environment;
    private final LeaveProperties leaveProperties;

    @Override
    public void run(final ApplicationArguments args) {
        if (isNonProductionProfileActive()) {
            return;
        }

        String jwtSecret = leaveProperties.getSecurity().getJwtSecret();
        assertCondition(jwtSecret != null && !jwtSecret.isBlank(), "AUTH_JWT_SECRET must be configured.");
        assertCondition(jwtSecret.length() >= 32, "AUTH_JWT_SECRET must be at least 32 characters.");
        assertCondition(hasText(environment.getProperty("spring.datasource.url")), "spring.datasource.url must be configured.");
        assertCondition(hasText(environment.getProperty("spring.datasource.username")), "spring.datasource.username must be configured.");
        assertCondition(hasText(environment.getProperty("spring.datasource.password")), "spring.datasource.password must be configured.");
        assertCondition(!environment.getProperty("spring.datasource.url", "").startsWith("jdbc:h2:"),
            "spring.datasource.url must not use H2 in prod.");
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
