package com.nexra.hrms.nexra.common.ops;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OperationsProductionReadinessValidatorTest {

    @Test
    void shouldSkipOutsideProd() {
        final MockEnvironment environment = new MockEnvironment()
            .withProperty("nexra.ops.backup.enabled", "false");

        assertThatCode(() -> new OperationsProductionReadinessValidator(environment).run(null))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldPassWithEnterpriseOperationsPostureInProd() {
        final MockEnvironment environment = validProdEnvironment();

        assertThatCode(() -> new OperationsProductionReadinessValidator(environment).run(null))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldFailWhenBackupsAreNotEnabledInProd() {
        final MockEnvironment environment = validProdEnvironment()
            .withProperty("nexra.ops.backup.enabled", "false");

        assertThatThrownBy(() -> new OperationsProductionReadinessValidator(environment).run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("nexra.ops.backup.enabled");
    }

    @Test
    void shouldFailWhenRecoveryPointObjectiveIsTooWeakInProd() {
        final MockEnvironment environment = validProdEnvironment()
            .withProperty("nexra.ops.rpo-minutes", "120");

        assertThatThrownBy(() -> new OperationsProductionReadinessValidator(environment).run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("nexra.ops.rpo-minutes");
    }

    private MockEnvironment validProdEnvironment() {
        final MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        return environment
            .withProperty("nexra.ops.backup.enabled", "true")
            .withProperty("nexra.ops.alerting.enabled", "true")
            .withProperty("management.endpoint.health.probes.enabled", "true")
            .withProperty("management.prometheus.metrics.export.enabled", "true")
            .withProperty("spring.flyway.enabled", "true")
            .withProperty("spring.jpa.open-in-view", "false")
            .withProperty("spring.jpa.hibernate.ddl-auto", "validate")
            .withProperty("server.forward-headers-strategy", "framework")
            .withProperty("nexra.ops.audit-retention-days", "365")
            .withProperty("nexra.ops.rpo-minutes", "60")
            .withProperty("nexra.ops.rto-minutes", "240");
    }
}
