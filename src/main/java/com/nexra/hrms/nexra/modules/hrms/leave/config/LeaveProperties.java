package com.nexra.hrms.nexra.modules.hrms.leave.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds leave module configuration properties.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@ConfigurationProperties(prefix = "app.leave")
public class LeaveProperties {

    private final Security security = new Security();

    public Security getSecurity() {
        return security;
    }

    /**
     * Security-related leave properties.
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

