package com.nexra.hrms.nexra.modules.hrms.expense;

import com.nexra.hrms.nexra.modules.hrms.expense.config.ExpenseProductionReadinessValidator;
import com.nexra.hrms.nexra.modules.hrms.expense.config.ExpenseProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ExpenseProductionReadinessValidator")
class ExpenseProductionReadinessValidatorTest {

    private Environment environment;
    private ExpenseProperties properties;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        properties = new ExpenseProperties();
        properties.getSecurity().setJwtSecret("12345678901234567890123456789012");
    }

    @Test
    @DisplayName("skips checks for test profile")
    void shouldSkipValidationForTestProfile() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

        ExpenseProductionReadinessValidator validator = new ExpenseProductionReadinessValidator(environment, properties);
        assertThatCode(() -> validator.run(null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("fails when jwt secret is weak in production")
    void shouldFailWhenJwtSecretTooShort() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("spring.datasource.url")).thenReturn("jdbc:mysql://db.internal:3306/nexra");
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:mysql://db.internal:3306/nexra");
        when(environment.getProperty("spring.datasource.username")).thenReturn("nexra");
        when(environment.getProperty("spring.datasource.password")).thenReturn("secret");
        properties.getSecurity().setJwtSecret("short-secret");

        ExpenseProductionReadinessValidator validator = new ExpenseProductionReadinessValidator(environment, properties);
        assertThatThrownBy(() -> validator.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jwt-secret must be at least 32 characters");
    }

    @Test
    @DisplayName("passes with production-safe settings")
    void shouldPassWithValidProductionSettings() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("spring.datasource.url")).thenReturn("jdbc:mysql://db.internal:3306/nexra");
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:mysql://db.internal:3306/nexra");
        when(environment.getProperty("spring.datasource.username")).thenReturn("nexra");
        when(environment.getProperty("spring.datasource.password")).thenReturn("secret");

        ExpenseProductionReadinessValidator validator = new ExpenseProductionReadinessValidator(environment, properties);
        assertThatCode(() -> validator.run(null)).doesNotThrowAnyException();
    }
}
