package com.nexra.hrms.nexra.modules.hrms.onboarding.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds onboarding module configuration properties.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@ConfigurationProperties(prefix = "app.onboarding")
public class OnboardingProperties {

    private final Security security = new Security();

    public Security getSecurity() {
        return security;
    }

    /**
     * Security properties for onboarding JWT validation.
     *
     * @author niteshjaitwar
     * @version 1.0
     */
    public static class Security {

        private String jwtSecret = "";

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(final String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }
    }
}
