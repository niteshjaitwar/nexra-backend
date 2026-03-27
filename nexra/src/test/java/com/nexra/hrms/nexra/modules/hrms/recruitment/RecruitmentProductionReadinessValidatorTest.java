package com.nexra.hrms.nexra.modules.hrms.recruitment;

import com.nexra.hrms.nexra.modules.hrms.recruitment.config.RecruitmentProductionReadinessValidator;
import com.nexra.hrms.nexra.modules.hrms.recruitment.config.RecruitmentProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("RecruitmentProductionReadinessValidator")
class RecruitmentProductionReadinessValidatorTest {

    private Environment environment;
    private RecruitmentProperties properties;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        properties = new RecruitmentProperties();
        properties.getSecurity().setJwtSecret("12345678901234567890123456789012");
    }

    @Test
    @DisplayName("skips checks for test profile")
    void shouldSkipValidationForTestProfile() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
        RecruitmentProductionReadinessValidator validator = new RecruitmentProductionReadinessValidator(environment, properties);
        assertThatCode(() -> validator.run(null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("fails when shared datasource is missing in production")
    void shouldFailWhenDatasourceUrlMissing() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("spring.datasource.url")).thenReturn("");
        when(environment.getProperty("spring.datasource.username")).thenReturn("nexra");
        when(environment.getProperty("spring.datasource.password")).thenReturn("secret");
        RecruitmentProductionReadinessValidator validator = new RecruitmentProductionReadinessValidator(environment, properties);
        assertThatThrownBy(() -> validator.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("spring.datasource.url must be configured");
    }

    @Test
    @DisplayName("fails when production datasource still uses H2")
    void shouldFailWhenDatasourceUsesH2() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("spring.datasource.url")).thenReturn("jdbc:h2:mem:nexra");
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:h2:mem:nexra");
        when(environment.getProperty("spring.datasource.username")).thenReturn("sa");
        when(environment.getProperty("spring.datasource.password")).thenReturn("secret");
        RecruitmentProductionReadinessValidator validator = new RecruitmentProductionReadinessValidator(environment, properties);
        assertThatThrownBy(() -> validator.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("must not use H2 in prod");
    }

    @Test
    @DisplayName("passes with production-safe shared datasource")
    void shouldPassWithValidSharedDatasource() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("spring.datasource.url")).thenReturn("jdbc:mysql://db.internal:3306/nexra");
        when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:mysql://db.internal:3306/nexra");
        when(environment.getProperty("spring.datasource.username")).thenReturn("nexra");
        when(environment.getProperty("spring.datasource.password")).thenReturn("secret");
        RecruitmentProductionReadinessValidator validator = new RecruitmentProductionReadinessValidator(environment, properties);
        assertThatCode(() -> validator.run(null)).doesNotThrowAnyException();
    }
}
