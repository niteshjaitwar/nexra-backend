package com.nexra.hrms.nexra.common.ops;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Component
public class OperationsProductionReadinessValidator implements ApplicationRunner {

    private final Environment environment;

    public OperationsProductionReadinessValidator(final Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(final ApplicationArguments args) {
        if (!isProdProfile()) {
            return;
        }

        assertCondition(getBoolean("nexra.ops.backup.enabled", false), "nexra.ops.backup.enabled must be true in prod.");
        assertCondition(getBoolean("nexra.ops.alerting.enabled", false), "nexra.ops.alerting.enabled must be true in prod.");
        assertCondition(getBoolean("management.endpoint.health.probes.enabled", false),
            "management.endpoint.health.probes.enabled must be true in prod.");
        assertCondition(getBoolean("management.prometheus.metrics.export.enabled", false),
            "management.prometheus.metrics.export.enabled must be true in prod.");
        assertCondition(getBoolean("spring.flyway.enabled", true), "spring.flyway.enabled must be true in prod.");
        assertCondition(!getBoolean("spring.jpa.open-in-view", true), "spring.jpa.open-in-view must be false in prod.");
        assertCondition("validate".equalsIgnoreCase(environment.getProperty("spring.jpa.hibernate.ddl-auto", "")),
            "spring.jpa.hibernate.ddl-auto must be validate in prod.");
        assertCondition(Set.of("framework", "native").contains(environment.getProperty("server.forward-headers-strategy", "")),
            "server.forward-headers-strategy must be framework or native in prod.");
        assertCondition(getInt("nexra.ops.audit-retention-days", 0) >= 365,
            "nexra.ops.audit-retention-days must be at least 365 in prod.");
        assertCondition(getInt("nexra.ops.rpo-minutes", Integer.MAX_VALUE) <= 60,
            "nexra.ops.rpo-minutes must be 60 or lower in prod.");
        assertCondition(getInt("nexra.ops.rto-minutes", Integer.MAX_VALUE) <= 240,
            "nexra.ops.rto-minutes must be 240 or lower in prod.");
    }

    private boolean isProdProfile() {
        return Arrays.stream(environment.getActiveProfiles()).anyMatch("prod"::equalsIgnoreCase);
    }

    private boolean getBoolean(final String key, final boolean defaultValue) {
        return Boolean.parseBoolean(environment.getProperty(key, Boolean.toString(defaultValue)));
    }

    private int getInt(final String key, final int defaultValue) {
        final String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private void assertCondition(final boolean condition, final String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
