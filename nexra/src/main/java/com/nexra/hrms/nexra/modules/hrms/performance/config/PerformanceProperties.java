package com.nexra.hrms.nexra.modules.hrms.performance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds performance module configuration properties.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@ConfigurationProperties(prefix = "app.performance")
public class PerformanceProperties {

    private final Security security = new Security();

    public Security getSecurity() {
        return security;
    }

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
