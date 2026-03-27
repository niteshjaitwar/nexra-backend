package com.nexra.hrms.nexra.modules.payroll;

import com.nexra.hrms.nexra.modules.payroll.config.PayrollProductionReadinessValidator;
import com.nexra.hrms.nexra.modules.payroll.config.PayrollProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("PayrollProductionReadinessValidator")
class PayrollProductionReadinessValidatorTest {

    private Environment environment;
    private PayrollProperties properties;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        properties = new PayrollProperties();
        properties.getSecurity().setJwtSecret("12345678901234567890123456789012");
        properties.getBrand().setCompanyName("Nexra HRMS");
        properties.getBrand().setBannerPath("/branding/nexra-banner.png");
        properties.getBrand().setWatermarkText("NEXRA HRMS");
    }

    @Test
    @DisplayName("skips checks for dev profile")
    void shouldSkipValidationForDevProfile() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});

        PayrollProductionReadinessValidator validator = new PayrollProductionReadinessValidator(environment, properties);
        assertThatCode(() -> validator.run(null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("fails when shared datasource credentials are missing in production")
    void shouldFailWhenDatasourcePasswordMissing() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("spring.datasource.url")).thenReturn("jdbc:mysql://db.internal:3306/nexra");
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:mysql://db.internal:3306/nexra");
        when(environment.getProperty("spring.datasource.username")).thenReturn("nexra");
        when(environment.getProperty("spring.datasource.password")).thenReturn("");

        PayrollProductionReadinessValidator validator = new PayrollProductionReadinessValidator(environment, properties);
        assertThatThrownBy(() -> validator.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("spring.datasource.password must be configured");
    }

    @Test
    @DisplayName("fails when production datasource still uses H2")
    void shouldFailWhenDatasourceUsesH2() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("spring.datasource.url")).thenReturn("jdbc:h2:mem:nexra");
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:h2:mem:nexra");
        when(environment.getProperty("spring.datasource.username")).thenReturn("sa");
        when(environment.getProperty("spring.datasource.password")).thenReturn("secret");

        PayrollProductionReadinessValidator validator = new PayrollProductionReadinessValidator(environment, properties);
        assertThatThrownBy(() -> validator.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("must not use H2 in prod");
    }

    @Test
    @DisplayName("passes with valid production configuration")
    void shouldPassWithValidProductionConfiguration() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("spring.datasource.url")).thenReturn("jdbc:mysql://db.internal:3306/nexra");
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:mysql://db.internal:3306/nexra");
        when(environment.getProperty("spring.datasource.username")).thenReturn("nexra");
        when(environment.getProperty("spring.datasource.password")).thenReturn("secret");

        PayrollProductionReadinessValidator validator = new PayrollProductionReadinessValidator(environment, properties);
        assertThatCode(() -> validator.run(null)).doesNotThrowAnyException();
    }
}
