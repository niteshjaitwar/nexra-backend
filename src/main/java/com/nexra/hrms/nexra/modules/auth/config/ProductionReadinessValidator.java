package com.nexra.hrms.nexra.modules.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Enforces fail-fast production safety checks to prevent insecure runtime configuration.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class ProductionReadinessValidator implements ApplicationRunner {

    private final Environment environment;
    private final AuthProperties authProperties;

    @Override
    public void run(final ApplicationArguments args) {
        if (isNonProductionProfileActive()) {
            return;
        }

        assertCondition(!isBlank(authProperties.getJwt().getSecret()), "AUTH_JWT_SECRET must be configured.");
        assertCondition(authProperties.getJwt().getSecret().length() >= 32, "AUTH_JWT_SECRET must be at least 32 characters.");
        assertCondition(!authProperties.isExposeVerificationTokenInResponse(),
            "app.auth.expose-verification-token-in-response must be false in prod.");
        assertCondition(!Boolean.parseBoolean(environment.getProperty("app.auth.bootstrap.enabled", "false")),
            "app.auth.bootstrap.enabled must be false in prod.");
        assertCondition(!authProperties.getOauth2().isEphemeralKeyEnabled(),
            "app.auth.oauth2.ephemeral-key-enabled must be false in prod.");
        assertCondition(!isBlank(authProperties.getOauth2().getIssuer()),
            "AUTH_OAUTH2_ISSUER must be configured in prod.");
        assertCondition(!isBlank(authProperties.getOauth2().getDefaultClientSecret()),
            "AUTH_OAUTH2_DEFAULT_CLIENT_SECRET must be configured in prod.");
        assertCondition(!isBlank(authProperties.getOauth2().getKeystoreLocation()),
            "AUTH_OAUTH2_KEYSTORE_LOCATION must be configured in prod.");
        assertCondition(!isBlank(authProperties.getOauth2().getKeystorePassword()),
            "AUTH_OAUTH2_KEYSTORE_PASSWORD must be configured in prod.");
        assertCondition(!isBlank(authProperties.getOauth2().getKeystoreKeyAlias()),
            "AUTH_OAUTH2_KEY_ALIAS must be configured in prod.");
        assertCondition(!isBlank(authProperties.getOauth2().getKeystoreKeyPassword()),
            "AUTH_OAUTH2_KEY_PASSWORD must be configured in prod.");
        assertCondition(authProperties.getSecurity().getCorsAllowedOrigins() != null
                && !authProperties.getSecurity().getCorsAllowedOrigins().isEmpty(),
            "app.auth.security.cors-allowed-origins must include at least one production origin.");
        assertCondition(authProperties.getSecurity().getCorsAllowedOrigins().stream().noneMatch(this::isLocalOrigin),
            "app.auth.security.cors-allowed-origins must not include localhost/127.0.0.1 in prod.");
        assertCondition(Boolean.parseBoolean(environment.getProperty("nexra.common.rate-limit.distributed-enabled", "false")),
            "nexra.common.rate-limit.distributed-enabled must be true in prod.");
        if (authProperties.getMail().isEnabled()) {
            assertCondition(!isBlank(authProperties.getMail().getFrom()),
                "AUTH_MAIL_FROM must be configured when mail is enabled in prod.");
        }
    }

    private boolean isNonProductionProfileActive() {
        for (String activeProfile : environment.getActiveProfiles()) {
            if ("dev".equalsIgnoreCase(activeProfile)
                || "local".equalsIgnoreCase(activeProfile)
                || "test".equalsIgnoreCase(activeProfile)
                || "e2e".equalsIgnoreCase(activeProfile)) {
                return true;
            }
        }
        return false;
    }

    private void assertCondition(final boolean condition, final String message) {
        if (!condition) {
            throw new IllegalStateException("Production configuration invalid: " + message);
        }
    }

    private boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    private boolean isLocalOrigin(final String origin) {
        if (isBlank(origin)) {
            return true;
        }
        final String normalized = origin.trim().toLowerCase();
        return normalized.contains("localhost") || normalized.contains("127.0.0.1");
    }
}
