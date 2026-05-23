package com.nexra.hrms.nexra.modules.auth;

import com.nexra.hrms.nexra.modules.auth.config.AuthProperties;
import com.nexra.hrms.nexra.modules.auth.config.ProductionReadinessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ProductionReadinessValidator")
class ProductionReadinessValidatorTest {

    private Environment environment;
    private AuthProperties properties;
    private ResourceLoader resourceLoader;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        resourceLoader = mock(ResourceLoader.class);
        properties = validProductionProperties();
        Resource resource = mock(Resource.class);
        when(resourceLoader.getResource("file:/tmp/auth-keystore.p12")).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.isReadable()).thenReturn(true);
        when(environment.getProperty("app.auth.bootstrap.enabled", "false")).thenReturn("false");
        when(environment.getProperty("nexra.common.rate-limit.distributed-enabled", "false")).thenReturn("true");
        when(environment.getProperty("spring.data.redis.host")).thenReturn("redis.internal");
        when(environment.getProperty("spring.data.redis.port")).thenReturn("6379");
    }

    @Test
    @DisplayName("skips checks for non-production profiles")
    void shouldSkipValidationForDevProfile() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});

        ProductionReadinessValidator validator = new ProductionReadinessValidator(environment, properties, resourceLoader);
        assertThatCode(() -> validator.run(null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("fails when JWT secret is missing in production")
    void shouldFailWhenJwtSecretMissing() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        properties.getJwt().setSecret("");

        ProductionReadinessValidator validator = new ProductionReadinessValidator(environment, properties, resourceLoader);
        assertThatThrownBy(() -> validator.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("AUTH_JWT_SECRET must be configured");
    }

    @Test
    @DisplayName("fails when OAuth issuer is missing in production")
    void shouldFailWhenIssuerMissing() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        properties.getOauth2().setIssuer(" ");

        ProductionReadinessValidator validator = new ProductionReadinessValidator(environment, properties, resourceLoader);
        assertThatThrownBy(() -> validator.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("AUTH_OAUTH2_ISSUER must be configured");
    }

    @Test
    @DisplayName("fails when default OAuth client secret is missing in production")
    void shouldFailWhenDefaultClientSecretMissing() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        properties.getOauth2().setDefaultClientSecret(" ");

        ProductionReadinessValidator validator = new ProductionReadinessValidator(environment, properties, resourceLoader);
        assertThatThrownBy(() -> validator.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("AUTH_OAUTH2_DEFAULT_CLIENT_SECRET must be configured");
    }

    @Test
    @DisplayName("fails when mail is enabled and sender is missing")
    void shouldFailWhenMailFromMissingWhileMailEnabled() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        properties.getMail().setEnabled(true);
        properties.getMail().setFrom(" ");

        ProductionReadinessValidator validator = new ProductionReadinessValidator(environment, properties, resourceLoader);
        assertThatThrownBy(() -> validator.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("AUTH_MAIL_FROM must be configured");
    }

    @Test
    @DisplayName("passes when production properties are fully configured")
    void shouldPassForValidProductionConfig() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("app.auth.bootstrap.enabled", "false")).thenReturn("false");

        ProductionReadinessValidator validator = new ProductionReadinessValidator(environment, properties, resourceLoader);
        assertThatCode(() -> validator.run(null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("fails when verification token is exposed in production")
    void shouldFailWhenExposeVerificationTokenInResponseTrue() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("app.auth.bootstrap.enabled", "false")).thenReturn("false");
        properties.setExposeVerificationTokenInResponse(true);

        ProductionReadinessValidator validator = new ProductionReadinessValidator(environment, properties, resourceLoader);
        assertThatThrownBy(() -> validator.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("expose-verification-token-in-response must be false in prod");
    }

    @Test
    @DisplayName("fails when bootstrap is enabled in production")
    void shouldFailWhenBootstrapEnabledInProd() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("app.auth.bootstrap.enabled", "false")).thenReturn("true");

        ProductionReadinessValidator validator = new ProductionReadinessValidator(environment, properties, resourceLoader);
        assertThatThrownBy(() -> validator.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("app.auth.bootstrap.enabled must be false in prod");
    }

    @Test
    @DisplayName("fails when ephemeral OAuth2 keys are enabled in production")
    void shouldFailWhenEphemeralKeyEnabledInProd() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        when(environment.getProperty("app.auth.bootstrap.enabled", "false")).thenReturn("false");
        properties.getOauth2().setEphemeralKeyEnabled(true);

        ProductionReadinessValidator validator = new ProductionReadinessValidator(environment, properties, resourceLoader);
        assertThatThrownBy(() -> validator.run(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ephemeral-key-enabled must be false in prod");
    }

    private AuthProperties validProductionProperties() {
        AuthProperties config = new AuthProperties();

        config.getJwt().setSecret("12345678901234567890123456789012");
        config.setExposeVerificationTokenInResponse(false);

        config.getOauth2().setEphemeralKeyEnabled(false);
        config.getOauth2().setIssuer("https://auth.nexra.local");
        config.getOauth2().setDefaultClientSecret("very-strong-client-secret");
        config.getOauth2().setKeystoreLocation("file:/tmp/auth-keystore.p12");
        config.getOauth2().setKeystorePassword("change-me");
        config.getOauth2().setKeystoreKeyAlias("nexra-auth");
        config.getOauth2().setKeystoreKeyPassword("change-me");

        config.getMail().setEnabled(false);
        config.getMail().setFrom("noreply@nexra.local");
        config.getSecurity().setRedisEnabled(true);
        config.getSecurity().setCorsAllowedOrigins(List.of("https://app.nexra.example", "https://hrms.nexra.example"));
        return config;
    }
}
