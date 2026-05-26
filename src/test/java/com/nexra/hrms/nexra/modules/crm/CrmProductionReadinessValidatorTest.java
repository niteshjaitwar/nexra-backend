package com.nexra.hrms.nexra.modules.crm;

import com.nexra.hrms.nexra.modules.crm.config.CrmProductionReadinessValidator;
import com.nexra.hrms.nexra.modules.crm.config.CrmProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CrmProductionReadinessValidator")
class CrmProductionReadinessValidatorTest {

    private Environment environment;
    private CrmProperties properties;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        properties = new CrmProperties();
        properties.setEnabled(true);
        properties.setEnforceAuth(true);
        properties.setMaxPageSize(100);
    }

    @Test
    @DisplayName("skips checks for test profile")
    void shouldSkipValidationForTestProfile() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        CrmProductionReadinessValidator validator = new CrmProductionReadinessValidator(environment, properties);
        assertThatCode(() -> validator.run(null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("fails when CRM is disabled in production")
    void shouldFailWhenCrmDisabledInProd() {
        properties.setEnabled(false);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        CrmProductionReadinessValidator validator = new CrmProductionReadinessValidator(environment, properties);
        assertThatThrownBy(() -> validator.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("nexra.crm.enabled must be true");
    }

    @Test
    @DisplayName("passes with valid production configuration")
    void shouldPassWithValidProductionConfiguration() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("spring.datasource.url")).thenReturn("jdbc:mysql://db.internal:3306/nexra");
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:mysql://db.internal:3306/nexra");
        when(environment.getProperty("spring.datasource.username")).thenReturn("nexra");
        when(environment.getProperty("spring.datasource.password")).thenReturn("secret");
        when(environment.getProperty("spring.jpa.open-in-view", "true")).thenReturn("false");
        when(environment.getProperty("spring.jpa.show-sql", "false")).thenReturn("false");
        when(environment.getProperty("spring.jpa.hibernate.ddl-auto", "")).thenReturn("validate");
        CrmProductionReadinessValidator validator = new CrmProductionReadinessValidator(environment, properties);
        assertThatCode(() -> validator.run(null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("fails when CRM page size is too large in production")
    void shouldFailWhenPageSizeTooLargeInProd() {
        properties.setMaxPageSize(500);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});

        CrmProductionReadinessValidator validator = new CrmProductionReadinessValidator(environment, properties);
        assertThatThrownBy(() -> validator.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("nexra.crm.max-page-size must not exceed 200");
    }

    @Test
    @DisplayName("fails when JPA open-in-view is enabled in production")
    void shouldFailWhenOpenInViewEnabledInProd() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("spring.jpa.open-in-view", "true")).thenReturn("true");

        CrmProductionReadinessValidator validator = new CrmProductionReadinessValidator(environment, properties);
        assertThatThrownBy(() -> validator.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("spring.jpa.open-in-view must be false");
    }
}
