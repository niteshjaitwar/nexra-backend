package com.nexra.hrms.nexra.modules.crm.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Fails startup when CRM production configuration is unsafe.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class CrmProductionReadinessValidator implements ApplicationRunner {

    private final Environment environment;
    private final CrmProperties properties;

    /**
     * Validates CRM production configuration at startup.
     *
     * @param args startup arguments.
     */
    @Override
    public void run(final ApplicationArguments args) {
        if (isNonProductionProfileActive()) {
            return;
        }
        assertCondition(properties.isEnabled(), "nexra.crm.enabled must be true in production.");
        assertCondition(properties.isEnforceAuth(), "nexra.crm.enforce-auth must be true in production.");
        assertCondition(properties.getMaxPageSize() > 0, "nexra.crm.max-page-size must be greater than 0.");
        assertCondition(properties.getMaxPageSize() <= 500, "nexra.crm.max-page-size must not exceed 500.");
        assertCondition(hasText(environment.getProperty("spring.datasource.url")), "spring.datasource.url must be configured.");
        assertCondition(!environment.getProperty("spring.datasource.url", "").startsWith("jdbc:h2:"),
            "spring.datasource.url must not use H2 in prod.");
        assertCondition(hasText(environment.getProperty("spring.datasource.username")), "spring.datasource.username must be configured.");
        assertCondition(hasText(environment.getProperty("spring.datasource.password")), "spring.datasource.password must be configured.");
    }

    /**
     * Determines whether a non-production profile is active.
     *
     * @return true when dev, test, or e2e is active.
     */
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

    /**
     * Checks whether a string contains non-whitespace text.
     *
     * @param value value to evaluate.
     * @return true when value contains text.
     */
    private boolean hasText(final String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Throws an IllegalStateException when a condition fails.
     *
     * @param condition evaluated condition.
     * @param message validation failure message.
     */
    private void assertCondition(final boolean condition, final String message) {
        if (!condition) {
            throw new IllegalStateException("Production configuration invalid: " + message);
        }
    }
}
